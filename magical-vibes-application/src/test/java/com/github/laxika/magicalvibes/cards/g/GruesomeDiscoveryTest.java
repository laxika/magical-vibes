package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruesomeDiscoveryTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting puts Gruesome Discovery on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Gruesome Discovery");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Without morbid, target player chooses and discards two cards")
    void targetChoosesDiscardsWithoutMorbid() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Peek");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .contains("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("With morbid, caster chooses two cards from target player's revealed hand")
    void casterChoosesDiscardsWithMorbid() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).discardMode()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactlyInAnyOrder(0, 1, 2);

        harness.handleCardChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .contains("Grizzly Bears", "Peek");
    }

    @Test
    @DisplayName("Morbid is checked at resolution time")
    void morbidCheckedAtResolution() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("With morbid and one card in hand, caster chooses that card")
    void morbidWithOneCardHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .contains("Peek");
    }

    @Test
    @DisplayName("With morbid and empty hand, no choice is prompted")
    void morbidWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("With morbid, wrong player cannot choose from revealed hand")
    void wrongPlayerCannotChooseWithMorbid() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new GruesomeDiscovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new GruesomeDiscovery(), new Forest(), new Peek())));
        harness.addMana(player1, ManaColor.BLACK, 4);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Gruesome Discovery", "Forest", "Peek");
    }
}
