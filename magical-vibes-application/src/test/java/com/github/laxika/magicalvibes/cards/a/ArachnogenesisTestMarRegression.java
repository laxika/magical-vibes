package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TerrifyingPresence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Arachnogenesis.class, GiantSpider.class, GrizzlyBears.class, TerrifyingPresence.class})
class ArachnogenesisTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Creates one Spider for each creature attacking the defending player")
    void createsSpidersForAttackers() {
        addAttacker(new GrizzlyBears());
        addAttacker(new GiantSpider());

        castArachnogenesis(player1);

        assertThat(findPermanents(player1, "Spider")).hasSize(2);
    }

    @Test
    @DisplayName("Prevents combat damage from non-Spiders but not from Spiders")
    void preventsNonSpiderCombatDamage() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        setAttackingYou(bear);
        setAttackingYou(spider);

        castArachnogenesis(player1);

        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, spider, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, false)).isFalse();
    }

    @Test
    @DisplayName("Non-Spider combat damage is prevented when combat resolves")
    void preventsCombatDamage() {
        harness.setLife(player1, 20);
        addAttacker(new GrizzlyBears());
        addAttacker(new GiantSpider());

        castArachnogenesis(player1);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    void createsNoTokensWithoutAttackersButStillPreventsDamage() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castArachnogenesis(player1);
        assertThat(findPermanents(player1, "Spider")).isEmpty();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isTrue();
    }

    @Test
    void attackingPlayerDoesNotCountTheirOwnAttackers() {
        addAttacker(new GrizzlyBears());
        castArachnogenesis(player2);
        assertThat(findPermanents(player2, "Spider")).isEmpty();
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    void spiderTokensDealDamageWhileNonSpiderBlockersDoNot() {
        Permanent attackingSpider = addAttacker(new GiantSpider());
        Permanent attackingBear = addAttacker(new GrizzlyBears());
        Permanent blockingBear = addCreatureReady(player1, new GrizzlyBears());
        castArachnogenesis(player1);
        Permanent token = findPermanents(player1, "Spider").getFirst();
        blockingBear.setBlocking(true);
        blockingBear.addBlockingTarget(0);
        blockingBear.addBlockingTargetId(attackingSpider.getId());
        token.setBlocking(true);
        token.addBlockingTarget(1);
        token.addBlockingTargetId(attackingBear.getId());

        harness.resolveCombatDamage();

        assertThat(attackingSpider.getMarkedDamage()).isZero();
        assertThat(attackingBear.getMarkedDamage()).isEqualTo(1);
        assertThat(token.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    void laterPreventionDoesNotEraseArachnogenesis() {
        Permanent bear = addAttacker(new GrizzlyBears());
        castArachnogenesis(player1);
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, bear.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    void arachnogenesisDoesNotEraseEarlierPrevention() {
        Permanent bear = addAttacker(new GrizzlyBears());
        addAttacker(new GiantSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, bear.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        castArachnogenesis(player1);
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    void createdTokensHaveReachAndSpecifiedSize() {
        addAttacker(new GrizzlyBears());
        castArachnogenesis(player1);

        Permanent token = findPermanents(player1, "Spider").getFirst();
        assertThat(gqs.hasKeyword(gd, token, Keyword.REACH)).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void preventionExpiresAfterTheTurn() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castArachnogenesis(player1);
        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isTrue();

        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isFalse();
    }

    private void castArachnogenesis(Player controller) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new Arachnogenesis()));
        harness.addMana(controller, ManaColor.GREEN, 1);
        harness.addMana(controller, ManaColor.COLORLESS, 2);
        harness.castInstant(controller, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player2, card);
        setAttackingYou(attacker);
        return attacker;
    }

    private void setAttackingYou(Permanent attacker) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
    }
}
