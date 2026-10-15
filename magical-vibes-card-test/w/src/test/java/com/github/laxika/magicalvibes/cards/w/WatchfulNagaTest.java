package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatchfulNaga.class, Colossapede.class, MagmaSpray.class})
class WatchfulNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting draws a card")
    void exertDraws() {
        addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInHand(player1, "Colossapede");
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent naga = addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(naga.isTapped()).isTrue();
        assertThat(naga.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert draws nothing and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent naga = addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Colossapede");
        assertThat(naga.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert skips only the next untap step of its controller")
    void exertRestrictionExpiresAfterControllersNextUntap() {
        Permanent naga = addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(naga.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(naga.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(naga.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining exert allows the normal next untap")
    void decliningExertAllowsNextUntap() {
        Permanent naga = addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        harness.performUntapStep(player1);
        assertThat(naga.isTapped()).isFalse();
        harness.assertNotInHand(player1, "Colossapede");
    }

    @Test
    @DisplayName("Removing an exerted Naga in response does not prevent the draw")
    void drawResolvesAfterSourceIsRemoved() {
        Permanent naga = addReadyNaga(player1);
        harness.setLibrary(player1, List.of(new Colossapede()));
        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(naga.getSkipUntapCount()).isEqualTo(1);
        harness.assertNotInHand(player1, "Colossapede");

        harness.castInstant(player2, 0, naga.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Watchful Naga");
        harness.assertInHand(player1, "Colossapede");
    }

    private Permanent addReadyNaga(Player player) {
        return addCreatureReady(player, new WatchfulNaga());
    }
}
