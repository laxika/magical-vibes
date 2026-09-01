package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepCavernBat.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Peek.class})
class DeepCavernBatTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a nonland card from the target opponent's hand")
    void mayExileNonlandCard() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land, instant)));

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
        Card card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));

        castAndResolveEtb();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("The exiled card returns when Deep-Cavern Bat leaves")
    void exiledCardReturnsWhenSourceLeaves() {
        Card instant = new Peek();
        harness.setHand(player2, List.of(instant));
        castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID batId = harness.getPermanentId(player1, "Deep-Cavern Bat");

        harness.passPriority(player1);
        harness.castInstant(player2, 0, batId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deep-Cavern Bat");
        harness.assertInHand(player2, "Peek");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals("Peek"));
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
