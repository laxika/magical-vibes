package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.r.Rebirth;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeweledBird.class, GrizzlyBears.class, HillGiant.class, Rebirth.class,
        Shatter.class, PullFromEternity.class})
class JeweledBirdTest extends BaseCardTest {

    @Test
    @DisplayName("Antes itself, clears the controller's other ante cards, and draws")
    void antesSelfClearsOtherOwnedAnteCardsAndDraws() {
        CardSetup setup = anteCardForPlayer1();
        harness.addToBattlefield(player1, new JeweledBird());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Jeweled Bird");
        harness.assertInGraveyard(player1, setup.antedCard().getName());
        harness.assertInHand(player1, setup.drawnCard().getName());
    }

    @Test
    @DisplayName("With no other ante cards, it still antes itself and draws")
    void antesSelfAndDrawsWithoutOtherAnteCards() {
        HillGiant drawnCard = new HillGiant();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new JeweledBird());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Jeweled Bird");
        harness.assertInHand(player1, drawnCard.getName());
    }

    @Test
    void cannotAnteAnOpponentOwnedBird() {
        CardSetup setup = anteCardForPlayer1();
        JeweledBird bird = new JeweledBird();
        bird.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, bird);
        List<?> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jeweled Bird");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(setup.antedCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bird);
        assertThat(gd.playerHands.get(player1.getId())).isEqualTo(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(setup.drawnCard());
    }

    @Test
    void doesNotRecoverAnteOrDrawWhenBirdLeavesBeforeResolution() {
        CardSetup setup = anteCardForPlayer1();
        var bird = harness.addToBattlefieldAndReturn(player1, new JeweledBird());
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, bird.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jeweled Bird");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(setup.antedCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(setup.drawnCard());
        harness.assertNotInHand(player1, setup.drawnCard().getName());
    }

    @Test
    void leavesOrdinaryExileAndOpponentsAnteUntouched() {
        GrizzlyBears firstAnte = new GrizzlyBears();
        HillGiant secondAnte = new HillGiant();
        GrizzlyBears opponentAnte = new GrizzlyBears();
        HillGiant drawnCard = new HillGiant();
        GrizzlyBears ordinaryExile = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        gd.addToAnte(player1.getId(), firstAnte);
        gd.addToAnte(player1.getId(), secondAnte);
        gd.addToAnte(player2.getId(), opponentAnte);
        harness.setExile(player1, List.of(ordinaryExile));
        harness.addToBattlefield(player1, new JeweledBird());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstAnte, secondAnte);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ordinaryExile)
                .doesNotContain(firstAnte, secondAnte);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentAnte);
        assertThat(gd.antedCardIds).contains(opponentAnte.getId())
                .doesNotContain(firstAnte.getId(), secondAnte.getId());
        harness.assertInHand(player1, drawnCard.getName());
    }

    @Test
    void antedBirdCannotBeTargetedAsAnExiledCard() {
        JeweledBird bird = new JeweledBird();
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addToBattlefield(player1, bird);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.antedCardIds).contains(bird.getId());
        harness.assertNotOnBattlefield(player1, "Jeweled Bird");
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bird.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private CardSetup anteCardForPlayer1() {
        GrizzlyBears antedCard = new GrizzlyBears();
        HillGiant drawnCard = new HillGiant();
        harness.setLibrary(player1, List.of(antedCard, drawnCard));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new Rebirth(), "{3}{G}{G}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        return new CardSetup(antedCard, drawnCard);
    }

    private record CardSetup(GrizzlyBears antedCard, HillGiant drawnCard) {
    }
}
