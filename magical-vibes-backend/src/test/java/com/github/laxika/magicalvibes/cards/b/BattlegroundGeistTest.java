package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattlegroundGeistTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Battleground Geist has static boost effect for Spirits")
    void hasCorrectStaticEffect() {
        BattlegroundGeist card = new BattlegroundGeist();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
        assertThat(effect.grantedKeywords()).isEmpty();
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    // ===== Static effect: buffs other Spirits you control =====

    @Test
    @DisplayName("Battleground Geist does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new BattlegroundGeist());

        Permanent geist = findPermanent(player1, "Battleground Geist");

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Spirit creatures")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.addToBattlefield(player2, new BattlegroundGeist());

        Permanent opponentGeist = findPermanent(player2, "Battleground Geist");

        // Opponent's Geist should have base 3/3, no buff from player1's Geist
        assertThat(gqs.getEffectivePower(gd, opponentGeist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGeist)).isEqualTo(3);
    }

    // ===== Multiple Battleground Geists =====

    @Test
    @DisplayName("Two Battleground Geists buff each other with +1/+0")
    void twoGeistsBuffEachOther() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.addToBattlefield(player1, new BattlegroundGeist());

        List<Permanent> geists = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Battleground Geist"))
                .toList();

        assertThat(geists).hasSize(2);
        for (Permanent geist : geists) {
            // 3 base + 1 from the other Geist = 4 power, toughness stays 3
            assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
        }
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Battleground Geist leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.addToBattlefield(player1, new BattlegroundGeist());

        List<Permanent> geists = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Battleground Geist"))
                .toList();

        // Both should be 4/3 (buffed by the other)
        assertThat(gqs.getEffectivePower(gd, geists.get(0))).isEqualTo(4);

        // Remove one Geist
        gd.playerBattlefields.get(player1.getId()).remove(geists.get(1));

        // Remaining Geist goes back to base 3/3
        assertThat(gqs.getEffectivePower(gd, geists.get(0))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geists.get(0))).isEqualTo(3);
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
