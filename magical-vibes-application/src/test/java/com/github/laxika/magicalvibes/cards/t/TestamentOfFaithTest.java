package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestamentOfFaithTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeActivation() {
        Permanent testament = addTestamentReady(player1);

        assertThat(gqs.isCreature(gd, testament)).isFalse();
    }

    @Test
    void activationUsesPaidXAndGrantsWallAndDefender() {
        Permanent testament = addTestamentReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, testament)).isTrue();
        assertThat(gqs.getEffectivePower(gd, testament)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, testament)).isEqualTo(3);
        assertThat(testament.getTransientSubtypes()).containsExactly(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, testament, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void animationWearsOffAtEndOfTurn() {
        Permanent testament = addTestamentReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, testament)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, testament)).isFalse();
        assertThat(testament.getTransientSubtypes()).isEmpty();
        assertThat(gqs.hasKeyword(gd, testament, Keyword.DEFENDER)).isFalse();
    }

    private Permanent addTestamentReady(Player player) {
        Permanent testament = new Permanent(new TestamentOfFaith());
        testament.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(testament);
        return testament;
    }
}
