package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Arachnogenesis.class, GrizzlyBears.class, CanopySpider.class})
class ArachnogenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Spider for each creature attacking you")
    void createsSpidersForAttackers() {
        addAttacker(player2, player1.getId(), new GrizzlyBears());
        addAttacker(player2, player1.getId(), new GrizzlyBears());

        castArachnogenesis();

        List<Permanent> spiders = findPermanents(player1, "Spider");
        assertThat(spiders).hasSize(2);
        assertThat(spiders).allSatisfy(spider -> {
            assertThat(spider.getEffectivePower()).isEqualTo(1);
            assertThat(spider.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Prevents combat damage from non-Spider creatures")
    void preventsCombatDamageFromNonSpiders() {
        Permanent attacker = addAttacker(player2, player1.getId(), new GrizzlyBears());

        castArachnogenesis();
        resolveCombat(player2);

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent combat damage from Spider creatures")
    void doesNotPreventCombatDamageFromSpiders() {
        Permanent attacker = addAttacker(player2, player1.getId(), new CanopySpider());

        castArachnogenesis();
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isLessThan(20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private void castArachnogenesis() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Arachnogenesis()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private Permanent addAttacker(Player player, UUID attackTarget, Card card) {
        Permanent attacker = addCreatureReady(player, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(attackTarget);
        return attacker;
    }
}
