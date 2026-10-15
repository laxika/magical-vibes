package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Poxwalkers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SzarekhTheSilentKing.class, Forest.class, Ornithopter.class, SkySkiff.class,
        SolRing.class, Poxwalkers.class})
class SzarekhTheSilentKingTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills three cards and may return a milled artifact creature")
    void attackingReturnsMilledArtifactCreature() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Ornithopter milledArtifactCreature = new Ornithopter();
        harness.setLibrary(player1, List.of(milledArtifactCreature, new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledArtifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking may return a milled Vehicle")
    void attackingReturnsMilledVehicle() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        SkySkiff milledVehicle = new SkySkiff();
        harness.setLibrary(player1, List.of(new Forest(), milledVehicle, new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledVehicle);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest");
    }

    @Test
    @DisplayName("Attacking does not offer a nonmatching milled card")
    void attackingDoesNotReturnNonmatchingCard() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card milledNonmatchingCard = new Forest();
        harness.setLibrary(player1, List.of(milledNonmatchingCard, new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(milledNonmatchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the return leaves all three milled cards in the graveyard")
    void mayDeclineReturn() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card eligible = new SzarekhTheSilentKing();
        List<Card> library = List.of(eligible, new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only one of multiple eligible milled cards can be returned")
    void returnsOnlyOneEligibleCard() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card first = new SzarekhTheSilentKing();
        Card second = new SzarekhTheSilentKing();
        Card third = new SzarekhTheSilentKing();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An earlier eligible milled card may be declined to choose a later one")
    void mayChooseLaterEligibleCard() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card first = new SzarekhTheSilentKing();
        Card second = new SzarekhTheSilentKing();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(first, second, land));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library is milled and its eligible card may still be returned")
    void shortLibraryStillAllowsReturn() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card eligible = new SzarekhTheSilentKing();
        harness.setLibrary(player1, List.of(eligible));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An eligible card already in the graveyard cannot be returned")
    void doesNotReturnPreviouslyMilledCard() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card oldCard = new SzarekhTheSilentKing();
        harness.setGraveyard(player1, List.of(oldCard));
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(oldCard, library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Neither a noncreature artifact nor a nonartifact creature is eligible")
    void requiresBothArtifactAndCreatureUnlessVehicle() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        List<Card> library = List.of(new SolRing(), new Poxwalkers(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attacking controller mills and receives the returned card")
    void millsAttackingControllersLibrary() {
        addCreatureReady(player2, new SzarekhTheSilentKing());
        Card eligible = new SzarekhTheSilentKing();
        Card untouched = new Forest();
        harness.setLibrary(player2, List.of(eligible, new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(untouched));
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking with an empty library does not offer a return or cause a loss")
    void emptyLibraryDoesNotOfferReturn() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }
}
