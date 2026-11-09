#!/usr/bin/env python3
"""Step 15, run 17z13: Bladecoil Serpent's three enters abilities each read the mana spent to cast it as they resolve,
from the permanent's record (the enters resolver no longer rebuilds their effects with the amount frozen in): a case
that resolves all three after a mixed payment. Idempotent."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"

path = TESTS / "b/src/test/java/com/github/laxika/magicalvibes/cards/b/BladecoilSerpentTest.java"
src = path.read_text()
if "eachAbilityReadsThePaymentAsItResolves" in src:
    print("already converted", path.name)
    sys.exit(0)
anchor = '''    @Test
    @DisplayName("Blue mana spent on X also contributes to card drawing")'''
if src.count(anchor) != 1:
    sys.exit("anchor not found")
case = '''    @Test
    @DisplayName("Each of the three abilities reads the payment as it resolves, after the others resolved")
    void eachAbilityReadsThePaymentAsItResolves() {
        harness.setHand(player1, List.of(new BladecoilSerpent()));
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.setHand(player2, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent serpent = findPermanent(player1, "Bladecoil Serpent");
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
            if (gd.interaction.activeInteraction() != null) {
                harness.handleCardChosen(player2, 0);
            }
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(serpent.getEffectivePower()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.TRAMPLE)).isTrue();
    }

'''
path.write_text(src.replace(anchor, case + anchor))
print("converted", path.name)
