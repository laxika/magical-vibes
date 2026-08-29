package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeteranCatharTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants double strike to a target Human")
    void grantsDoubleStrikeToTargetHuman() {
        addReady(player1, new VeteranCathar());
        Permanent human = addReady(player2, new ThrabenInspector());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        addReady(player1, new VeteranCathar());
        Permanent human = addReady(player1, new ThrabenInspector());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, human, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-Human creature")
    void cannotTargetNonHumanCreature() {
        addReady(player1, new VeteranCathar());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Human");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
