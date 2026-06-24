const fs = require('fs');

const inputFile = './arcade.txt';
const numFiles = 12;
const tableName = 'juego_detalle';

// Leer archivo
const content = fs.readFileSync(inputFile, 'utf-8');
const lines = content.split(/\r?\n/).filter(l => l.trim() !== '');

// Columnas desde el header
const columns = lines[0].split('\t').map(c => c.trim());
const dataLines = lines.slice(1);

const totalRows = dataLines.length;
const rowsPerFile = Math.ceil(totalRows / numFiles);

console.log(`📊 Total registros: ${totalRows}`);
console.log(`📄 Registros por archivo: ${rowsPerFile}`);

for (let i = 0; i < numFiles; i++) {
    const start = i * rowsPerFile;
    const end = Math.min(start + rowsPerFile, totalRows);

    if (start >= totalRows) break;

    const inserts = dataLines.slice(start, end).map(line => {
        const fields = line.split('\t').map(v => {
            if (v.trim() === '') return 'NULL';
            return `'${v.replace(/'/g, "''")}'`;
        });

        return `INSERT INTO ${tableName} (${columns.join(', ')}) VALUES (${fields.join(', ')});`;
    });

    const outputFile = `arcade-part${i + 1}.sql`;
    fs.writeFileSync(outputFile, inserts.join('\n'), 'utf-8');

    console.log(`✅ Creado: ${outputFile} (${end - start} INSERTs)`);
}
