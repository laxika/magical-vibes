package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HowlOfTheHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next instant or sorcery spell once without raid")
    void copiesNextSpellOnceWithoutRaid() {
        castHowlAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        castDivinationAndResolve();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Copies the next instant or sorcery spell twice with raid")
    void copiesNextSpellTwiceWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castHowlAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(2);

        castDivinationAndResolve();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    private void castHowlAndResolve() {
        harness.setHand(player1, List.of(new HowlOfTheHorde(), new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void castDivinationAndResolve() {
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
    }
}
