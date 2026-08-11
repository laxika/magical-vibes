package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RushOfBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures and grants lifelink to your Warriors")
    void boostsCreaturesAndGrantsLifelinkToWarriors() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DwynensElite());
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new DwynensElite());

        castRushOfBattle();

        assertThat(warrior.getEffectivePower()).isEqualTo(4);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(3);
        assertThat(nonWarrior.getEffectivePower()).isEqualTo(4);
        assertThat(nonWarrior.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentWarrior.getEffectivePower()).isEqualTo(2);
        assertThat(opponentWarrior.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWarrior, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentWarrior, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DwynensElite());
        castRushOfBattle();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(2);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.LIFELINK)).isFalse();
    }

    private void castRushOfBattle() {
        harness.setHand(player1, List.of(new RushOfBattle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
