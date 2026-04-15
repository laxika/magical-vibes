#!/bin/bash
# Usage: ./scripts/scryfall-lookup.sh <SET_CODE> <COLLECTOR_NUMBER>
# Example: ./scripts/scryfall-lookup.sh DKA 29
#
# Returns only the fields needed for card implementation:
#   name, mana_cost, type_line, oracle_text, power, toughness, keywords
#
# For double-faced cards (DFCs), prints both faces separately.

set -euo pipefail

if [ $# -ne 2 ]; then
    echo "Usage: $0 <SET_CODE> <COLLECTOR_NUMBER>"
    echo "Example: $0 DKA 29"
    exit 1
fi

SET_CODE="$1"
COLLECTOR_NUMBER="$2"

curl -s \
    -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" \
    -H "Accept: application/json" \
    "https://api.scryfall.com/cards/search?q=set:${SET_CODE}+cn:${COLLECTOR_NUMBER}&format=json" \
| python -c "
import sys, json
data = json.load(sys.stdin)
if data.get('object') == 'error':
    print('Error:', data.get('details', 'Unknown error'))
    sys.exit(1)
card = data['data'][0]
fields = ['name', 'mana_cost', 'type_line', 'oracle_text', 'power', 'toughness', 'keywords']

def print_fields(obj):
    for f in fields:
        if f in obj and obj[f]:
            val = obj[f]
            if isinstance(val, list):
                val = ', '.join(val) if val else '(none)'
            print(f'{f}: {val}')

# Double-faced cards have card_faces
if 'card_faces' in card:
    # Print top-level keywords if present (e.g. Transform)
    if card.get('keywords'):
        print('keywords: ' + ', '.join(card['keywords']))
        print()
    for i, face in enumerate(card['card_faces']):
        label = 'FRONT' if i == 0 else 'BACK'
        print(f'=== {label} FACE ===')
        print_fields(face)
        print()
else:
    print_fields(card)
"
