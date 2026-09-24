package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Arachnogenesis.class, GiantSpider.class, GrizzlyBears.class})
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

    private void castArachnogenesis(Player controller) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new Arachnogenesis()));
        harness.addMana(controller, ManaColor.GREEN, 1);
        harness.addMana(controller, ManaColor.COLORLESS, 2);
        harness.castInstant(controller, 0);
        harness.passBothPriorities();
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
