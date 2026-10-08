package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.p.PsychicSurgery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VarragothBloodskySire.class, Mistwalker.class, FrostBite.class})
class VarragothBloodskySireTest extends BaseCardTest {

    @Test
    @DisplayName("Boast lets the target player search and put a card on top of their library")
    void boastSearchesTargetPlayersLibrary() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.setLibrary(player2, List.of(new Mistwalker(), new FrostBite()));
        addBoastMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Mistwalker", "Frost Bite");

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Frost Bite");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Boast requires Varragoth to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new VarragothBloodskySire());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.setLibrary(player2, List.of(new Mistwalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast can target its controller and requires finding a card")
    void boastCanTargetControllerAndCannotFailToFind() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        Card chosen = new FrostBite();
        harness.setLibrary(player1, List.of(new Mistwalker(), chosen));
        addBoastMana();

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
    }

    @Test
    @DisplayName("Boast resolves against an empty library and still uses the activation")
    void boastAgainstEmptyLibraryStillCountsAsActivation() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast resolves after Varragoth leaves the battlefield")
    void boastResolvesWithoutItsSource() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        Card chosen = new Mistwalker();
        harness.setLibrary(player2, List.of(chosen));
        addBoastMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(varragoth);
        gd.playerGraveyards.get(player1.getId()).add(varragoth.getCard());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(chosen);
    }

    @Test
    @CardUsed(PsychicSurgery.class)
    @DisplayName("Searching an empty library still triggers Psychic Surgery from the shuffle")
    void emptyLibraryStillTriggersShuffleAbilities() {
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.addToBattlefield(player1, new PsychicSurgery());
        harness.setLibrary(player2, List.of());
        addBoastMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
