package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReasonBelieveTest extends BaseCardTest {

    @Test
    @DisplayName("Reason scries 3")
    void reasonScries3() {
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new GrizzlyBears(), new Island()));
        harness.setHand(player1, List.of(new ReasonBelieve()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reason"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Believe may put a creature from library top onto the battlefield, then exiles")
    void believePutsCreatureOntoBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.setGraveyard(player1, List.of(new ReasonBelieve()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Reason") || c.getName().equals("Believe"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reason"));
    }

    @Test
    @DisplayName("Believe puts declined creature into hand")
    void believeDeclinedCreatureGoesToHand() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.setGraveyard(player1, List.of(new ReasonBelieve()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reason"));
    }

    @Test
    @DisplayName("Believe puts non-creature top card into hand with no choice")
    void believeNonCreatureGoesToHand() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new ReasonBelieve()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reason"));
    }

    @Test
    @DisplayName("Believe requires sorcery timing")
    void believeRequiresSorceryTiming() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new ReasonBelieve()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Believe fails without enough mana")
    void believeFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new ReasonBelieve()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
