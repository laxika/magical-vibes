package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanDaringTraveler.class, GrizzlyBears.class})
class KellanDaringTravelerTest extends BaseCardTest {

    @Test
    @DisplayName("Journey On creates one Map plus one per artifact opponents control")
    void journeyOnCreatesMapsAndAllowsCreatureFromExile() {
        KellanDaringTraveler card = new KellanDaringTraveler();
        Card artifact = new Card();
        artifact.setType(CardType.ARTIFACT);
        harness.addToBattlefield(player2, artifact);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.MAP)))
                .hasSize(2);
        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == card);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("Attacking puts a revealed creature with mana value three or less into hand")
    void attackingPutsMatchingCreatureIntoHand() {
        addCreatureReady(player1, new KellanDaringTraveler());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking offers a nonmatching revealed card for the graveyard")
    void attackingMayPutNonmatchingCardIntoGraveyard() {
        addCreatureReady(player1, new KellanDaringTraveler());
        Card nonCreature = new Card();
        nonCreature.setType(CardType.INSTANT);
        harness.setLibrary(player1, List.of(nonCreature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonCreature);
    }
}
