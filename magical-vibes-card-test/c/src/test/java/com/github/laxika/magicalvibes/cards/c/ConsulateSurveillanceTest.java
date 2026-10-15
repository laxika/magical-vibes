package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.t.TerrorOfTheFairgrounds;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsulateSurveillance.class, TerrorOfTheFairgrounds.class, MoggFanatic.class})
class ConsulateSurveillanceTest extends BaseCardTest {

    @Test
    void entersWithFourEnergyCounters() {
        harness.castFromHand(player1, new ConsulateSurveillance(), "{3}{W}");

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void paysTwoEnergyOnActivationAndPreventsChosenSourceDamage() {
        addSurveillance();
        Permanent source = addCreatureReady(player2, new TerrorOfTheFairgrounds());
        gd.playerEnergyCounters.put(player1.getId(), 4);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId())).contains(source.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotActivateWithoutTwoEnergyCounters() {
        addSurveillance();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");
    }

    private Permanent addSurveillance() {
        return harness.addToBattlefieldAndReturn(player1, new ConsulateSurveillance());
    }

    @Test
    void preventsChosenCreatureCombatDamageButNotAnotherCreature() {
        addSurveillance();
        Permanent chosen = addCreatureReady(player2, new TerrorOfTheFairgrounds());
        addCreatureReady(player2, new TerrorOfTheFairgrounds());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void canChooseSacrificedSourceWhoseDamageAbilityIsOnStack() {
        addSurveillance();
        Permanent source = harness.addToBattlefieldAndReturn(player2, new MoggFanatic());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(source.getId());
        harness.handlePermanentChosen(player1, source.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

}
