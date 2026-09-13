package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JiangYanggu.class, GrizzlyBears.class, Forest.class})
class JiangYangguTest extends BaseCardTest {

    @Test
    void plusOneBoostsTargetCreatureUntilEndOfTurn() {
        Permanent jiang = addReadyJiang(player1, 4);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(jiang.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void minusOneCreatesLegendaryMowuWhenNoneIsControlled() {
        Permanent jiang = addReadyJiang(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent mowu = findPermanent(player1, "Mowu");
        assertThat(gqs.getEffectivePower(gd, mowu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mowu)).isEqualTo(3);
        assertThat(jiang.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusOneDoesNotCreateAnotherMowu() {
        addReadyJiang(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent secondJiang = addReadyJiang(player1, 4);
        int secondJiangIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondJiang);

        harness.activateAbility(player1, secondJiangIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mowu")).hasSize(1);
        assertThat(secondJiang.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusFiveScalesPumpByLandsAndGrantsTrample() {
        Permanent jiang = addReadyJiang(player1, 5);
        Permanent creature = addReadyCreature(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(jiang.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void loyaltyAbilitiesRequireCreatureTargets() {
        Permanent jiang = addReadyJiang(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, jiang.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyJiang(Player player, int loyalty) {
        Permanent permanent = new Permanent(new JiangYanggu());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
