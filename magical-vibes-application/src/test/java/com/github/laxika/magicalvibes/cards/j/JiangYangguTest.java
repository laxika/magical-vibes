package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DrownInShapelessness;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JiangYanggu.class, GrizzlyBears.class, Forest.class, DrownInShapelessness.class})
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
        Permanent jiang = addReadyJiang(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mowu")).hasSize(1);
        assertThat(jiang.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
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

    @Test
    void plusOneCanBoostAnOpponentsCreature() {
        addReadyJiang(player1, 4);
        Permanent creature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void opponentsMowuDoesNotPreventTokenCreation() {
        addReadyJiang(player2, 4);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        addReadyJiang(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mowu")).hasSize(1);
        assertThat(findPermanents(player2, "Mowu")).hasSize(1);
    }

    @Test
    void minusFiveCountsControllersLandsAtResolutionAndLocksInTheBoost() {
        addReadyJiang(player1, 6);
        Permanent creature = addReadyCreature(player2);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void minusOneCreatesReplacementWhenMowuLeavesBeforeResolution() {
        addReadyJiang(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent originalMowu = findPermanent(player1, "Mowu");
        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player2, List.of(new DrownInShapelessness()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, originalMowu.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mowu")).hasSize(1);
        assertThat(findPermanent(player1, "Mowu").getId()).isNotEqualTo(originalMowu.getId());
    }

    @Test
    void minusFiveDoesNotAffectCreatureThatLeftBeforeResolution() {
        addReadyJiang(player1, 6);
        Permanent creature = addReadyCreature(player1);
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.setHand(player2, List.of(new DrownInShapelessness()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void minusFiveGrantsTrampleWithNoLands() {
        addReadyJiang(player1, 5);
        Permanent creature = addReadyCreature(player1);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Jiang Yanggu");
    }

    private Permanent addReadyJiang(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JiangYanggu());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
