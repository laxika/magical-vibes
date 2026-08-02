package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class ParagonOfOpenGravesTest extends BaseCardTest {

    @Test
    @DisplayName("Other black creatures you control get +1/+1")
    void buffsOtherBlackCreaturesYouControl() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, child)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, child)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nonblack or opponent creatures")
    void onlyBuffsOwnBlackCreatures() {
        addReady(player1, new ParagonOfOpenGraves());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentChild = addReady(player2, new ChildOfNight());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentChild)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentChild)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating grants deathtouch to another black creature you control")
    void grantsDeathtouchToAnotherBlackCreature() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nonblack creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent opponentChild = addReady(player2, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another black creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another black creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentChild.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another black creature");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
