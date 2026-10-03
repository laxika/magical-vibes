package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuroraShifter.class, AirElemental.class, GrizzlyBears.class})
class AuroraShifterTest extends BaseCardTest {

    @Test
    void gainsEnergyEqualToCombatDamageDealtToPlayer() {
        addCreatureReady(player1, new AuroraShifter());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void payingEnergyCopiesAnotherCreatureAndRetainsCombatDamageTrigger() {
        Permanent aurora = addCreatureReady(player1, new AuroraShifter());
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, aurora, Keyword.FLYING)).isFalse();
        harness.handlePermanentChosen(player1, elemental.getId());
        assertThat(gqs.hasKeyword(gd, aurora, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, aurora, Keyword.FLYING)).isTrue();

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void cannotCopyWithoutEnoughEnergy() {
        Permanent aurora = addCreatureReady(player1, new AuroraShifter());
        addCreatureReady(player1, new AirElemental());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gqs.hasKeyword(gd, aurora, Keyword.FLYING)).isFalse();
    }

    @Test
    void beginningOfCombatTargetMustBeAnotherCreatureYouControl() {
        Permanent aurora = addCreatureReady(player1, new AuroraShifter());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        advanceToBeginningOfCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownBear.getId())
                .doesNotContain(aurora.getId(), opposingBear.getId());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void canPayEnergyEvenWhenThereIsNoOtherCreatureToCopy() {
        addCreatureReady(player1, new AuroraShifter());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningPaymentDoesNotChooseATargetOrSpendEnergy() {
        Permanent aurora = addCreatureReady(player1, new AuroraShifter());
        addCreatureReady(player1, new AirElemental());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        advanceToBeginningOfCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aurora, Keyword.FLYING)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerAtBeginningOfOpponentsCombat() {
        addCreatureReady(player1, new AuroraShifter());
        addCreatureReady(player1, new AirElemental());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }
}
