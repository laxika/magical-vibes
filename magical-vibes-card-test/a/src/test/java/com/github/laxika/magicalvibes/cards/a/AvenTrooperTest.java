package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AvenTrooper.class)
class AvenTrooperTest extends BaseCardTest {

    @Test
    void discardingACardAndPayingManaBoostsAvenTrooper() {
        Permanent trooper = addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of(new AvenTrooper()));
        prepareAbilityActivation();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Aven Trooper");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent trooper = addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of(new AvenTrooper()));
        prepareAbilityActivation();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of());
        prepareAbilityActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardIsPaidBeforeTheBoostResolves() {
        Permanent trooper = addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of(new AvenTrooper()));
        prepareAbilityActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Aven Trooper");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(3);
    }

    @Test
    void repeatedActivationsAccumulateAndOnlyBoostTheirSource() {
        Permanent trooper = addCreatureReady(player1, new AvenTrooper());
        Permanent otherTrooper = addCreatureReady(player1, new AvenTrooper());
        harness.setHand(player1, List.of(new AvenTrooper(), new AvenTrooper()));
        prepareAbilityActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherTrooper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherTrooper)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent trooper = harness.addToBattlefieldAndReturn(player1, new AvenTrooper());
        trooper.setSummoningSick(true);
        trooper.tap();
        harness.setHand(player1, List.of(new AvenTrooper()));
        prepareAbilityActivation();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trooper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, trooper)).isEqualTo(3);
        assertThat(trooper.isTapped()).isTrue();
    }

    private void prepareAbilityActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
