package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaduceusStaffOfHermes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCapitolineTriad.class, GrizzlyBears.class, CaduceusStaffOfHermes.class, TurnToFrog.class})
class TheCapitolineTriadTest extends BaseCardTest {

    @Test
    void tenHistoricCardsAllowCastingWithoutMana() {
        harness.setGraveyard(player1, java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> (Card) new TheCapitolineTriad()).toList());
        harness.setHand(player1, List.of(new TheCapitolineTriad()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void duplicateSelectionCannotPayTheThirtyManaValueCost() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        Card first = new TheCapitolineTriad();
        Card second = new TheCapitolineTriad();
        Card third = new TheCapitolineTriad();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.emblems).isEmpty();
    }

    @Test
    void laterBasePowerAndToughnessSetterOverridesEmblem() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card first = new TheCapitolineTriad();
        Card second = new TheCapitolineTriad();
        Card third = new TheCapitolineTriad();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(9);

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void reducesItsOwnCostWithoutAnotherTriadOnTheBattlefield() {
        harness.setGraveyard(player1, List.of(new TheCapitolineTriad(), new CaduceusStaffOfHermes()));
        harness.setHand(player1, List.of(new TheCapitolineTriad()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void triadOnBattlefieldDoesNotDiscountOtherHistoricSpells() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        harness.setGraveyard(player1, List.of(new TheCapitolineTriad(), new TheCapitolineTriad()));
        harness.setHand(player1, List.of(new CaduceusStaffOfHermes()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsHistoricCardsDoNotReduceItsCost() {
        harness.setGraveyard(player2, List.of(new TheCapitolineTriad()));
        harness.setHand(player1, List.of(new TheCapitolineTriad()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithLessThanThirtyHistoricManaValue() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        harness.setGraveyard(player1, List.of(new TheCapitolineTriad(), new TheCapitolineTriad()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.emblems).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void nonHistoricManaValueCannotPayActivationCost() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        harness.setGraveyard(player1, List.of(new TheCapitolineTriad(), new TheCapitolineTriad(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.emblems).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    @Test
    void selectedCardsMustMeetThresholdEvenWhenMoreAreAvailable() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        Card first = new TheCapitolineTriad();
        Card second = new TheCapitolineTriad();
        Card third = new TheCapitolineTriad();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.emblems).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void mayExileMoreThanThirtyAndCostIsPaidBeforeEmblemResolves() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        Card first = new TheCapitolineTriad();
        Card second = new TheCapitolineTriad();
        Card third = new TheCapitolineTriad();
        Card fourth = new TheCapitolineTriad();
        harness.setGraveyard(player1, List.of(first, second, third, fourth));
        harness.activateAbility(player1, 0, 0, null, null);

        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.emblems).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        var laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(harness.getGameQueryService().getEffectivePower(gd, laterCreature)).isEqualTo(9);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, laterCreature)).isEqualTo(9);
    }

    @Test
    void newlyCreatedEmblemOverridesEarlierBasePowerAndToughnessSetter() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(1);
        Card first = new TheCapitolineTriad();
        Card second = new TheCapitolineTriad();
        Card third = new TheCapitolineTriad();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    void historicCardsInGraveyardReduceHistoricSpellCosts() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        harness.setGraveyard(player1, List.of(
                new TheCapitolineTriad(), new TheCapitolineTriad(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TheCapitolineTriad()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void nonHistoricCardsDoNotReduceHistoricSpellCosts() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TheCapitolineTriad()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilingHistoricCardsCreatesAnEmblemThatSetsYourCreaturesToNineNine() {
        harness.addToBattlefield(player1, new TheCapitolineTriad());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card first = new TheCapitolineTriad();
        Card second = new TheCapitolineTriad();
        Card third = new TheCapitolineTriad();
        Card nonHistoric = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third, nonHistoric));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);

        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        GameQueryService queryService = harness.getGameQueryService();
        assertThat(queryService.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(queryService.getEffectiveToughness(gd, creature)).isEqualTo(9);
        assertThat(queryService.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(queryService.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonHistoric);
    }
}
