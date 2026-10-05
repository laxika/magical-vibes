package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldfuryStrider;
import com.github.laxika.magicalvibes.cards.t.TinkersTote;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanDaringTraveler.class, GrizzlyBears.class,
        GoldfuryStrider.class, KutzilsFlanker.class, TinkersTote.class})
class KellanDaringTravelerTest extends BaseCardTest {

    @Test
    @DisplayName("Journey On creates one Map plus one per opponent controlling an artifact")
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

        assertThat(countPermanents(player1, "Map")).isEqualTo(2);
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

    @Test
    @DisplayName("Multiple artifacts controlled by one opponent still create only two Maps")
    void journeyOnCountsOpponentsRatherThanArtifacts() {
        harness.addToBattlefield(player2, new TinkersTote());
        harness.addToBattlefield(player2, new GoldfuryStrider());
        harness.setHand(player1, List.of(new KellanDaringTraveler()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Map")).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifacts controlled only by the caster do not increase the Map count")
    void journeyOnIgnoresOwnArtifacts() {
        harness.addToBattlefield(player1, new TinkersTote());
        harness.setHand(player1, List.of(new KellanDaringTraveler()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Map")).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with mana value exactly three goes into hand")
    void attackingPutsThreeManaCreatureIntoHand() {
        addCreatureReady(player1, new KellanDaringTraveler());
        KutzilsFlanker creature = new KutzilsFlanker();
        harness.setLibrary(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature with mana value greater than three may go into the graveyard")
    void attackingMayPutExpensiveCreatureIntoGraveyard() {
        addCreatureReady(player1, new KellanDaringTraveler());
        GoldfuryStrider creature = new GoldfuryStrider();
        harness.setLibrary(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the graveyard choice leaves a noncreature on top")
    void attackingMayLeaveNoncreatureOnTop() {
        addCreatureReady(player1, new KellanDaringTraveler());
        TinkersTote artifact = new TinkersTote();
        harness.setLibrary(player1, List.of(artifact));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("An empty library produces no card movement or choice")
    void attackingWithEmptyLibraryDoesNothing() {
        addCreatureReady(player1, new KellanDaringTraveler());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
