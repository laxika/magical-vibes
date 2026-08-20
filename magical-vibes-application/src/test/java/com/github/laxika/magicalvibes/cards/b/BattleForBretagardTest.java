package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattleForBretagardTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I and II create Human Warrior and Elf Warrior tokens")
    void chaptersCreateWarriorTokens() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        Permanent saga = saga();
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        assertThat(countOf("Human Warrior")).isEqualTo(1);

        advanceToNextChapter();
        assertThat(countOf("Elf Warrior")).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III copies a chosen distinct-name set of artifact and creature tokens")
    void chapterIIICopiesChosenDistinctTokens() {
        harness.addToBattlefield(player1, new BattleForBretagard());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, token("Bear", CardType.CREATURE, 2, 2));
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, token("Bear", CardType.CREATURE, 2, 2));
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, token("Treasure", CardType.ARTIFACT, 0, 0));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, token("Elf", CardType.CREATURE, 1, 1));
        harness.addToBattlefield(player1, token("Land Token", CardType.LAND, 0, 0));

        Permanent saga = saga();
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstBear.getId(), treasure.getId(), elf.getId()));

        assertThat(countOf("Bear")).isEqualTo(3);
        assertThat(countOf("Treasure")).isEqualTo(2);
        assertThat(countOf("Elf")).isEqualTo(2);
        assertThat(countOf("Land Token")).isEqualTo(1);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent saga() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Battle for Bretagard"))
                .findFirst()
                .orElseThrow();
    }

    private long countOf(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }

    private static Card token(String name, CardType type, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(type == CardType.CREATURE ? CardColor.GREEN : null);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
