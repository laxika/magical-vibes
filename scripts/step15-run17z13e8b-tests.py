#!/usr/bin/env python3
"""Step 15, run 17z13e8b: the card cases for the draw readers' abilities read whole.

- `GixianPuppeteerTest.drawAbilityIsOneStackObject`: "each opponent loses 2 life and you gain 2 life" is one ability of
  two effects, one object on the stack (CR 603.3), the draw readers reading an ability whole; nothing happens until it
  resolves.

Idempotent. Usage: step15-run17z13e8b-tests.py [REPOSITORY_ROOT].
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent)
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

GIXIAN = '''
    @Test
    @DisplayName("The second-draw ability is one object on the stack: the loss and the gain resolve together")
    void drawAbilityIsOneStackObject() {
        harness.addToBattlefield(player1, new GixianPuppeteer());
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        drawAndResolveTrigger(player1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
'''
CASES = [("g", "GixianPuppeteerTest", GIXIAN, "drawAbilityIsOneStackObject",
          "    private void killInCombat() {")]


def main():
    for letter, name, case, method, anchor in CASES:
        path = TESTS / letter / PKG / letter / f"{name}.java"
        text = path.read_text()
        if method in text:
            continue
        if anchor not in text:
            raise SystemExit(f"{name}: the anchor is gone")
        text = text.replace(anchor, case.lstrip("\n") + "\n" + anchor, 1)
        path.write_text(text)
        print(f"edited {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
