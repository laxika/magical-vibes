package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheSecondSun;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarvoDeepOperative.class, Forest.class, GrizzlyBears.class, WorldspineWurm.class, SphinxOfTheSecondSun.class})
class MarvoDeepOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking and winning a clash draws and offers a spell with mana value eight or less")
    void winningClashDrawsAndOffersFreeCast() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell with mana value greater than eight is not offered after a won clash")
    void highManaValueSpellIsNotOffered() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new WorldspineWurm()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(WorldspineWurm.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the clash does not draw or offer a free cast")
    void losingClashDoesNothing() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equal mana values do not win the clash")
    void tiedClashDoesNothing() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new MarvoDeepOperative()));
        harness.setLibrary(player2, List.of(new MarvoDeepOperative()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A spell with mana value exactly eight can be cast for free")
    void manaValueEightCanBeCast() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new SphinxOfTheSecondSun()));
        harness.setLibrary(player2, List.of(new MarvoDeepOperative()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();

        harness.assertInHand(player1, "Sphinx of the Second Sun");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sphinx of the Second Sun");
        harness.assertNotInHand(player1, "Sphinx of the Second Sun");
    }

    @Test
    @DisplayName("Declining the free cast still draws the card")
    void decliningCastKeepsDrawnCard() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new SphinxOfTheSecondSun()));
        harness.setLibrary(player2, List.of(new MarvoDeepOperative()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Sphinx of the Second Sun");
        harness.assertNotOnBattlefield(player1, "Sphinx of the Second Sun");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Bottoming the revealed card draws the next card and can cast a card already in hand")
    void bottomingCardDrawsNextCardAndAllowsExistingHandSpell() {
        addCreatureReady(player1, new MarvoDeepOperative());
        SphinxOfTheSecondSun revealed = new SphinxOfTheSecondSun();
        MarvoDeepOperative nextCard = new MarvoDeepOperative();
        harness.setLibrary(player1, List.of(revealed, nextCard));
        harness.setLibrary(player2, List.of(new MarvoDeepOperative()));
        harness.setHand(player1, List.of(new SphinxOfTheSecondSun()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(nextCard).doesNotContain(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sphinx of the Second Sun");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Winning a clash initiated by the opponent triggers Marvo without attacking")
    void winningOpponentsClashDrawsAndOffersCast() {
        addCreatureReady(player1, new MarvoDeepOperative());
        addCreatureReady(player2, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new SphinxOfTheSecondSun()));
        harness.setLibrary(player2, List.of(new MarvoDeepOperative()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(0));
        resolveClashKeepingCardsOnTop();

        harness.assertInHand(player1, "Sphinx of the Second Sun");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sphinx of the Second Sun");
    }

    @Test
    @DisplayName("Drawing a land after winning does not offer to cast it")
    void drawnLandIsNotOffered() {
        addCreatureReady(player1, new MarvoDeepOperative());
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(new SphinxOfTheSecondSun(), nextCard));
        harness.setLibrary(player2, List.of(new MarvoDeepOperative()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A revealed land wins against an opponent with an empty library")
    void revealedLandWinsAgainstEmptyLibrary() {
        addCreatureReady(player1, new MarvoDeepOperative());
        Forest revealed = new Forest();
        harness.setLibrary(player1, List.of(revealed));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    private void resolveClashKeepingCardsOnTop() {
        resolveAllTriggers();
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry placement) {
            var choosingPlayer = placement.playerId().equals(player1.getId()) ? player1 : player2;
            gs.handleInteractionAnswer(gd, choosingPlayer,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            resolveAllTriggers();
        }
    }
}
