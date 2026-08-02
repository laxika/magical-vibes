package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MeditationPuzzleTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 8 life")
    void gainsEightLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Can use convoke to help cast the spell")
    void castsWithConvoke() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID convokeCreatureId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreatureId));

        Permanent convokeCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }
}
