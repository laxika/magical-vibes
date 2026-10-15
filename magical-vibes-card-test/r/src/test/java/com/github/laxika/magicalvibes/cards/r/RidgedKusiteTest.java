package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PouncingWurm;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RidgedKusite.class, PouncingWurm.class, UrborgTombOfYawgmoth.class})
class RidgedKusiteTest extends BaseCardTest {

    @Test
    void canTargetItselfAndDiscardALand() {
        Permanent kusite = addCreatureReady(player1, new RidgedKusite());
        harness.setHand(player1, List.of(new UrborgTombOfYawgmoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, kusite.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(kusite.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Urborg, Tomb of Yawgmoth");
        assertThat(kusite.getPowerModifier()).isZero();
        assertThat(kusite.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(kusite.getPowerModifier()).isEqualTo(1);
        assertThat(kusite.getToughnessModifier()).isZero();
        assertThat(kusite.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent kusite = harness.addToBattlefieldAndReturn(player1, new RidgedKusite());
        kusite.setSummoningSick(true);
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kusite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(kusite.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent kusite = addCreatureReady(player1, new RidgedKusite());
        kusite.tap();
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kusite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneBlackMana() {
        Permanent kusite = addCreatureReady(player1, new RidgedKusite());
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kusite.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kusite.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresDiscardingACard() {
        addCreatureReady(player1, new RidgedKusite());
        Permanent target = addCreatureReady(player2, new PouncingWurm());
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
    }

    @Test
    void resolvingAbilityBoostsAndGrantsFirstStrikeUntilEndOfTurn() {
        Permanent kusite = addCreatureReady(player1, new RidgedKusite());
        Permanent target = addCreatureReady(player2, new PouncingWurm());
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(kusite.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Pouncing Wurm");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new RidgedKusite());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent target = addCreatureReady(player2, new PouncingWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent kusite = addCreatureReady(player1, new RidgedKusite());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        harness.setHand(player1, List.of(new PouncingWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(kusite.isTapped()).isFalse();
    }
}
