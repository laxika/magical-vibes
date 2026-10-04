package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Aurochs;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionWhite;
import com.github.laxika.magicalvibes.cards.r.RitualOfSubdual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({EssenceFilter.class, CircleOfProtectionWhite.class, RitualOfSubdual.class, Aurochs.class})
class EssenceFilterTest extends BaseCardTest {

    private void castEssenceFilter(int mode) {
        harness.setHand(player1, List.of(new EssenceFilter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, mode == 0
                ? "Destroy all enchantments" : "Destroy all nonwhite enchantments");
    }

    @Test
    @DisplayName("Mode 0 destroys every enchantment regardless of color")
    void allEnchantmentsMode() {
        harness.addToBattlefield(player1, new CircleOfProtectionWhite());
        harness.addToBattlefield(player2, new RitualOfSubdual());
        harness.addToBattlefield(player2, new Aurochs());

        castEssenceFilter(0);

        harness.assertNotOnBattlefield(player1, "Circle of Protection: White");
        harness.assertNotOnBattlefield(player2, "Ritual of Subdual");
        harness.assertOnBattlefield(player2, "Aurochs");
    }

    @Test
    @DisplayName("Mode 1 destroys only nonwhite enchantments")
    void nonwhiteEnchantmentsMode() {
        harness.addToBattlefield(player1, new CircleOfProtectionWhite());
        harness.addToBattlefield(player2, new CircleOfProtectionWhite());
        harness.addToBattlefield(player2, new RitualOfSubdual());

        castEssenceFilter(1);

        harness.assertOnBattlefield(player1, "Circle of Protection: White");
        harness.assertOnBattlefield(player2, "Circle of Protection: White");
        harness.assertNotOnBattlefield(player2, "Ritual of Subdual");
    }

    @Test
    @DisplayName("The destruction choice is made on resolution after players can respond")
    void destructionChoiceIsMadeOnResolution() {
        harness.setHand(player1, List.of(new EssenceFilter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addToBattlefield(player1, new CircleOfProtectionWhite());
        harness.castSorcery(player1, 0);

        harness.assertOnBattlefield(player1, "Circle of Protection: White");
        harness.assertNotInGraveyard(player1, "Essence Filter");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Circle of Protection: White");
        harness.handleListChoice(player1, "Destroy all enchantments");

        harness.assertInGraveyard(player1, "Circle of Protection: White");
        harness.assertInGraveyard(player1, "Essence Filter");
    }

    @Test
    @DisplayName("Nonwhite destruction affects both players and leaves nonenchantments alone")
    void nonwhiteDestructionAffectsBothPlayers() {
        harness.addToBattlefield(player1, new RitualOfSubdual());
        harness.addToBattlefield(player2, new RitualOfSubdual());
        harness.addToBattlefield(player1, new Aurochs());
        harness.addToBattlefield(player2, new Aurochs());

        castEssenceFilter(1);

        harness.assertInGraveyard(player1, "Ritual of Subdual");
        harness.assertInGraveyard(player2, "Ritual of Subdual");
        harness.assertOnBattlefield(player1, "Aurochs");
        harness.assertOnBattlefield(player2, "Aurochs");
    }

    @Test
    @DisplayName("Essence Filter goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castEssenceFilter(0);

        harness.assertInGraveyard(player1, "Essence Filter");
    }
}
