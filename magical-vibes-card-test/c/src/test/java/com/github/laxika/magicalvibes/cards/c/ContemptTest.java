package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
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
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Contempt.class, GrizzlyBears.class, Mountain.class, Disenchant.class})
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
        resolveAllTriggers();

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
        resolveAllTriggers();

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

    @Test
    @DisplayName("Destroying Contempt in response to its attack trigger still returns the attacker")
    void destroyedAuraDoesNotPreventCreatureReturn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castContempt(bears);
        Permanent aura = findPermanent(player1, "Contempt");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.castAndResolveInstant(player1, 0, aura.getId());
            resolveAllTriggers();
        });
        harness.assertInGraveyard(player1, "Contempt");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Contempt");
    }

    @Test
    @DisplayName("The end-of-combat return waits for its delayed triggered ability to resolve")
    void returnUsesStackAtEndOfCombat() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castContempt(bears);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Contempt");

        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Contempt");
    }

    @Test
    @DisplayName("An unenchanted attacker does not cause Contempt or its creature to return")
    void otherCreatureAttackingDoesNotTriggerReturn() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castContempt(enchanted);

        declareAttackers(player1, List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(enchanted, attacker);
        harness.assertOnBattlefield(player1, "Contempt");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Contempt");
    }

    private void castContempt(Permanent creature) {
        harness.setHand(player1, List.of(new Contempt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
