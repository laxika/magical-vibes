package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudArcher.class, SilverWyvern.class, SkyshroudFalcon.class, YouthfulKnight.class})
class SkyshroudArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to give a target creature with flying -1/-1 until end of turn")
    void givesFlyingCreatureMinusOneMinusOne() {
        addReadyArcher(player1);
        Permanent wyvern = addCreatureReady(player2, new SilverWyvern());

        harness.activateAbility(player1, 0, null, wyvern.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wyvern)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wyvern)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a flying creature you control")
    void canTargetOwnFlyingCreature() {
        addReadyArcher(player1);
        Permanent wyvern = addCreatureReady(player1, new SilverWyvern());

        harness.activateAbility(player1, 0, null, wyvern.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wyvern)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wyvern)).isEqualTo(2);
    }

    @Test
    @DisplayName("-1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addReadyArcher(player1);
        Permanent wyvern = addCreatureReady(player2, new SilverWyvern());

        harness.activateAbility(player1, 0, null, wyvern.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wyvern)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wyvern)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addReadyArcher(player1);
        Permanent knight = addCreatureReady(player2, new YouthfulKnight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-1/-1 can kill a small flying creature")
    void canKillSmallFlyingCreature() {
        addReadyArcher(player1);
        Permanent falcon = addCreatureReady(player2, new SkyshroudFalcon());

        harness.activateAbility(player1, 0, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(falcon.getId()));
    }

    private Permanent addReadyArcher(Player player) {
        return addCreatureReady(player, new SkyshroudArcher());
    }
}
