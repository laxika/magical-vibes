package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoaningSpirit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UncheckedGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a non-Spirit creature +4/+4 without trample")
    void boostsNonSpiritWithoutTrample() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castUncheckedGrowth("Grizzly Bears");

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gives a Spirit creature +4/+4 and trample")
    void boostsSpiritWithTrample() {
        harness.addToBattlefield(player1, new MoaningSpirit());
        castUncheckedGrowth("Moaning Spirit");

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MoaningSpirit());
        castUncheckedGrowth("Moaning Spirit");

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void castUncheckedGrowth(String targetName) {
        harness.setHand(player1, List.of(new UncheckedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
