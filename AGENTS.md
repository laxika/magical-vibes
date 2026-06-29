READ CLAUDE.md FIRST!!!

- For card implementation context, run `powershell -ExecutionPolicy Bypass -File scripts/implement-card-context.ps1 <SET_CODE> <COLLECTOR_NUMBER> [ClassName]` before inspecting broad docs. The script prints:
  - Scryfall summary and reprint check
  - **New effect needed** (yes/no/maybe)
  - **Oracle effect mapping** with domain/handler file paths
  - **Token-saving plan** (what to read, what to skip, grep keywords)
  - **Composite card skeleton** when multiple oracle fragments match
  - **Reference constructor** excerpt from the closest implemented card
  - **Minimal test checklist** and focused Gradle test command
- Re-run with `-Full` only when no template/skeleton is available and you need Effect Doc Hits / Existing Usages.
