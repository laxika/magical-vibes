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
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
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
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gqs.hasKeyword(gd, aurora, Keyword.FLYING)).isFalse();
    }

    @Test
    void beginningOfCombatTargetMustBeAnotherCreatureYouControl() {
        Permanent aurora = addCreatureReady(player1, new AuroraShifter());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownBear.getId())
                .doesNotContain(aurora.getId(), opposingBear.getId());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
