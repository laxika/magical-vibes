package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepCavernBat.class, Forest.class, ArmoredKincaller.class, Abrade.class})
class DeepCavernBatTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a nonland card from the target opponent's hand")
    void mayExileNonlandCard() {
        Card creature = new ArmoredKincaller();
        Card land = new Forest();
        Card instant = new Abrade();
        harness.setHand(player2, List.of(creature, land, instant));

        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals their hand"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, instant);
    }

    @Test
    @DisplayName("Declining the ETB leaves the opponent's hand unchanged")
    void mayDecline() {
        Card card = new ArmoredKincaller();
        harness.setHand(player2, List.of(card));

        castAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("The exiled card returns when Deep-Cavern Bat leaves")
    void exiledCardReturnsWhenSourceLeaves() {
        Card instant = new Abrade();
        harness.setHand(player2, List.of(instant));
        castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        UUID batId = harness.getPermanentId(player1, "Deep-Cavern Bat");

        harness.castInstant(player2, 0, 0, batId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deep-Cavern Bat");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(instant);
    }

    @Test
    @DisplayName("Deep-Cavern Bat can target only an opponent")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new DeepCavernBat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("The hand is inspected before deciding whether to exile")
    void looksAtHandBeforeExileDecision() {
        harness.setHand(player2, List.of(new ArmoredKincaller()));

        castAndResolveEtb();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("The hand is inspected even when exile is declined")
    void looksAtHandWhenExileDeclined() {
        Card card = new ArmoredKincaller();
        harness.setHand(player2, List.of(card));

        castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("A hand containing only lands has no legal exile choice")
    void cannotExileLand() {
        Card land = new Forest();
        harness.setHand(player2, List.of(land));

        castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(land);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)).isNull();
    }

    @Test
    @DisplayName("An empty hand leaves no card to exile")
    void emptyHand() {
        harness.setHand(player2, List.of());

        castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)).isNull();
    }

    @Test
    @DisplayName("Leaving before the ETB resolves prevents exile but still inspects the hand")
    void sourceLeavesBeforeTriggerResolves() {
        Card card = new ArmoredKincaller();
        harness.setHand(player2, List.of(new Abrade(), card));
        harness.setHand(player1, List.of(new DeepCavernBat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        UUID batId = harness.getPermanentId(player1, "Deep-Cavern Bat");
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, 0, batId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Deep-Cavern Bat");
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.RevealedHandChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(card);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeepCavernBat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
