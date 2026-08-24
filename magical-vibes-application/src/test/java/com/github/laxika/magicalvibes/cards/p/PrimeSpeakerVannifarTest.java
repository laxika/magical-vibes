package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimeSpeakerVannifarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice a creature and search for a creature with exactly one higher mana value")
    void sacrificeCreatureSearchesForCreatureWithOneHigherManaValue() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GoldMyr(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(card -> card.getName().equals("Gold Myr"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Gold Myr");
    }

    @Test
    @DisplayName("Cannot sacrifice Prime Speaker Vannifar itself")
    void cannotSacrificeSourceItself() {
        addVannifarReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addVannifarReady(player1);
        addCreature(player1, new LlanowarElves());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addVannifarReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addVannifarReady(Player player) {
        harness.addToBattlefield(player, new PrimeSpeakerVannifar());
        gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Prime Speaker Vannifar"))
                .findFirst()
                .orElseThrow()
                .setSummoningSick(false);
    }

    private void addCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(player, card);
        gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(card.getName()))
                .reduce((first, second) -> second)
                .orElseThrow()
                .setSummoningSick(false);
    }
}
