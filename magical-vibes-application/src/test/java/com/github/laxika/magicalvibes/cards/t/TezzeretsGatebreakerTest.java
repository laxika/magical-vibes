package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AerialEngineer;
import com.github.laxika.magicalvibes.cards.a.ArcaneEncyclopedia;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TezzeretsGatebreaker.class, AerialEngineer.class, ArcaneEncyclopedia.class,
        GreenwoodSentinel.class, Island.class, Shock.class, Swamp.class})
class TezzeretsGatebreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability offers blue or artifact cards")
    void offersBlueOrArtifactCard() {
        AerialEngineer aerialEngineer = new AerialEngineer();
        ArcaneEncyclopedia encyclopedia = new ArcaneEncyclopedia();
        setupTopCards(List.of(new Shock(), aerialEngineer, new Island(), encyclopedia, new Swamp()));

        castGatebreaker();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(aerialEngineer.getId(), encyclopedia.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Choosing a blue or artifact card puts it into hand and bottoms the rest")
    void choosesEligibleCardAndBottomsRest() {
        AerialEngineer aerialEngineer = new AerialEngineer();
        ArcaneEncyclopedia encyclopedia = new ArcaneEncyclopedia();
        List<Card> topCards = List.of(aerialEngineer, new Shock(), encyclopedia, new Island(), new Swamp());
        setupTopCards(topCards);

        castGatebreaker();
        harness.handleMultipleCardsChosen(player1, List.of(encyclopedia.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(encyclopedia);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards.stream()
                        .filter(card -> card != encyclopedia)
                        .toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing it makes only your creatures unblockable this turn")
    void sacrificeMakesOwnCreaturesUnblockable() {
        harness.addToBattlefield(player1, new TezzeretsGatebreaker());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tezzeret's Gatebreaker");
        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    @Test
    void mayChooseMulticoloredBlueCardFromShortLibrary() {
        AerialEngineer engineer = new AerialEngineer();
        Island island = new Island();
        setupTopCards(List.of(engineer, island));

        castGatebreaker();
        harness.handleMultipleCardsChosen(player1, List.of(engineer.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(engineer);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineEligibleCardAndBottomAllFiveBelowUntouchedCards() {
        List<Card> lookedAt = List.of(new AerialEngineer(), new ArcaneEncyclopedia(),
                new Shock(), new Island(), new Swamp());
        GreenwoodSentinel untouched = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), untouched));

        castGatebreaker();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryWithNoEligibleCardsFinishesWithoutChoice() {
        List<Card> cards = List.of(new Island(), new Swamp(), new Shock());
        setupTopCards(cards);

        castGatebreaker();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryFinishesWithoutChoice() {
        setupTopCards(List.of());

        castGatebreaker();

        harness.assertOnBattlefield(player1, "Tezzeret's Gatebreaker");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creaturesEnteringAfterResolutionCannotBeBlockedUntilTurnEnds() {
        harness.addToBattlefield(player1, new TezzeretsGatebreaker());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Tezzeret's Gatebreaker");
        harness.assertNotOnBattlefield(player1, "Tezzeret's Gatebreaker");
        harness.passBothPriorities();

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
    }

    private void castGatebreaker() {
        harness.setHand(player1, List.of(new TezzeretsGatebreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
