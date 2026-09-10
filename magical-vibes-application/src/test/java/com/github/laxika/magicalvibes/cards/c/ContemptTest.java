package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Contempt.class, GrizzlyBears.class, Mountain.class})
class ContemptTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature attacks, Contempt does not return it immediately")
    void attackDoesNotReturnImmediately() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castContempt(bears);

        declareAttackers(player1, List.of(0));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Contempt");
    }

    @Test
    @DisplayName("The enchanted creature and Contempt return to their owners' hands at end of combat")
    void returnsCreatureAndAuraAtEndOfCombat() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castContempt(bears);

        declareAttackers(player1, List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Contempt");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Contempt");
    }

    @Test
    @DisplayName("Returns the enchanted creature and Contempt to their respective owners' hands")
    void returnsToTheirOwnersHands() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castContempt(bears);

        declareAttackers(player2, List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInHand(player1, "Contempt");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Contempt");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Contempt cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Contempt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castContempt(Permanent creature) {
        harness.setHand(player1, List.of(new Contempt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
