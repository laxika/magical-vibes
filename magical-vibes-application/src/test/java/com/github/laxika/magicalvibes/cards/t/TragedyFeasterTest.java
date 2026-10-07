package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TragedyFeaster.class, GrizzlyBears.class, Shock.class, Swamp.class, ProdigalPyromancer.class})
class TragedyFeasterTest extends BaseCardTest {

    @Test
    @DisplayName("At end step with no life gained, controller must sacrifice a permanent")
    void sacrificesPermanentWhenNoLifeGained() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // lifeGainedThisTurn left at 0

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve end-step trigger → begins sacrifice choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No sacrifice happens when you gained life this turn")
    void noSacrificeWhenLifeGained() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve end-step trigger → condition not met, does nothing

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ward triggers when an opponent targets Tragedy Feaster")
    void wardTriggersOnOpponentSpell() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, feaster.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Tragedy Feaster");
    }

    @Test
    void canSacrificeItself() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, feaster.getId());

        harness.assertNotOnBattlefield(player1, "Tragedy Feaster");
        harness.assertInGraveyard(player1, "Tragedy Feaster");
    }

    @Test
    void canSacrificeALand() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, swamp.getId());

        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Tragedy Feaster");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TragedyFeaster());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Tragedy Feaster");
    }

    @Test
    void triggersEvenWhenLifeWasAlreadyGained() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Tragedy Feaster");
    }

    @Test
    void gainingLifeAfterTriggeringPreventsSacrifice() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Tragedy Feaster");
    }

    @Test
    void opponentsLifeGainDoesNotPreventSacrifice() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, feaster.getId());

        harness.assertInGraveyard(player1, "Tragedy Feaster");
    }

    @Test
    void lifeGainStillCountsAfterLosingMoreLife() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 5, "life loss");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Tragedy Feaster");
    }

    @Test
    void wardCountersSpellWhenOpponentHasNoCardToDiscard() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, feaster.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(feaster.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void decliningWardDiscardCountersSpell() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, feaster.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(feaster.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void payingWardDiscardAllowsSpellToResolve() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, feaster.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(feaster.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Tragedy Feaster");
    }

    @Test
    void wardDoesNotTriggerForControllersOwnSpell() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, feaster.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(feaster.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent feaster = harness.addToBattlefieldAndReturn(player1, new TragedyFeaster());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.setHand(player2, List.of());

        harness.activateAbility(player2, 0, null, feaster.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(feaster.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
