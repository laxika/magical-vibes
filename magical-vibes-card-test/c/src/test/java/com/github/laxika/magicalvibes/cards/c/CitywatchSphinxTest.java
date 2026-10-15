package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SteamVents;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CitywatchSphinx.class, SteamVents.class, VernadiShieldmate.class})
class CitywatchSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, surveils two")
    void deathSurveilsTwo() {
        Card topCard = new VernadiShieldmate();
        Card secondCard = new SteamVents();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        Permanent sphinx = addReadySphinx(player1);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(topCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .doesNotContain(topCard, secondCard);
    }

    @Test
    @DisplayName("Another creature's death does not trigger it")
    void anotherCreatureDeathDoesNotTrigger() {
        addReadySphinx(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil can keep both cards and reverse their order above the untouched library")
    void keepsBothInChosenOrder() {
        Card first = new VernadiShieldmate();
        Card second = new SteamVents();
        Card third = new CitywatchSphinx();
        harness.setLibrary(player1, List.of(first, second, third));
        Permanent sphinx = addReadySphinx(player1);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sphinx.getCard());
    }

    @Test
    @DisplayName("Surveil can put one card into the graveyard and keep the other on top")
    void splitsCardsBetweenLibraryAndGraveyard() {
        Card first = new VernadiShieldmate();
        Card second = new SteamVents();
        Card third = new CitywatchSphinx();
        harness.setLibrary(player1, List.of(first, second, third));
        Permanent sphinx = addReadySphinx(player1);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sphinx.getCard(), first);
    }

    @Test
    @DisplayName("Surveil two with only one card looks at that card and can keep it")
    void surveilsShortLibrary() {
        Card onlyCard = new SteamVents();
        harness.setLibrary(player1, List.of(onlyCard));
        Permanent sphinx = addReadySphinx(player1);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sphinx.getCard());
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without asking for a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent sphinx = addReadySphinx(player1);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sphinx.getCard());
    }

    @Test
    @DisplayName("The dying Sphinx's controller surveils their own library")
    void opponentSurveilsTheirOwnLibrary() {
        Card untouched = new SteamVents();
        Card first = new VernadiShieldmate();
        Card second = new CitywatchSphinx();
        harness.setLibrary(player1, List.of(untouched));
        harness.setLibrary(player2, List.of(first, second));
        Permanent sphinx = addReadySphinx(player2);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil.playerId()).isEqualTo(player2.getId());
        assertThat(surveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(sphinx.getCard(), first, second);
    }

    private Permanent addReadySphinx(Player player) {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player, new CitywatchSphinx());
        sphinx.setSummoningSick(false);
        return sphinx;
    }
}
