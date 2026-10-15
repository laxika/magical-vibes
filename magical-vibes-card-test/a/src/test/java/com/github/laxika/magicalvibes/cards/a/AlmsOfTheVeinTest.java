package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlmsOfTheVein.class, RavensCrime.class})
class AlmsOfTheVeinTest extends BaseCardTest {

    private AlmsOfTheVein discardViaRavensCrime() {
        AlmsOfTheVein alms = new AlmsOfTheVein();
        harness.setHand(player1, List.of(alms));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return alms;
    }

    @Test
    @DisplayName("Target opponent loses 3 life and you gain 3 life")
    void drainsTargetOpponent() {
        harness.setHand(player1, List.of(new AlmsOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new AlmsOfTheVein()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Discarding Alms of the Vein offers a madness cast")
    void acceptingMadnessDrainsTargetOpponent() {
        AlmsOfTheVein alms = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(alms.getId()));
    }

    @Test
    @DisplayName("Discarding with madness puts the card in exile before the cast choice")
    void discardedCardWaitsInExile() {
        AlmsOfTheVein alms = discardViaRavensCrime();

        assertThat(gd.findExiledCard(alms.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(alms);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(alms);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining madness puts the card into its owner's graveyard without draining life")
    void decliningMadnessPutsCardInGraveyard() {
        AlmsOfTheVein alms = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(alms.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(alms);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unpayable madness cast puts the card into its owner's graveyard")
    void unpayableMadnessPutsCardInGraveyard() {
        AlmsOfTheVein alms = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(alms.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(alms);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
