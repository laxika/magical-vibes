package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TheCapitolineTriad.class, GrizzlyBears.class})
class TheCapitolineTriadTest extends BaseCardTest {

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
