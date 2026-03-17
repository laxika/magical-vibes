package com.github.laxika.magicalvibes.cards.g;

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

class GallowsWardenTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Gallows Warden has static boost effect for Spirits")
    void hasCorrectStaticEffect() {
        GallowsWarden card = new GallowsWarden();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(0);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).isEmpty();
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    // ===== Static effect: buffs other Spirits you control =====

    @Test
    @DisplayName("Other Spirit creatures you control get +0/+1")
    void buffsOtherSpirits() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player1, new GhostWarden());

        Permanent ghostWarden = findPermanent(player1, "Ghost Warden");

        // Ghost Warden is 1/1 base + 0/1 from Gallows Warden = 1/2
        assertThat(gqs.getEffectivePower(gd, ghostWarden)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ghostWarden)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gallows Warden does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new GallowsWarden());

        Permanent warden = findPermanent(player1, "Gallows Warden");

        // Base 3/3, no self-buff
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Spirit creatures")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player2, new GhostWarden());

        Permanent opponentSpirit = findPermanent(player2, "Ghost Warden");

        // Ghost Warden is 1/1 base, no buff from opponent's Gallows Warden
        assertThat(gqs.getEffectivePower(gd, opponentSpirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSpirit)).isEqualTo(1);
    }

    // ===== Multiple Gallows Wardens =====

    @Test
    @DisplayName("Two Gallows Wardens buff each other with +0/+1")
    void twoWardensBuffEachOther() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player1, new GallowsWarden());

        List<Permanent> wardens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gallows Warden"))
                .toList();

        assertThat(wardens).hasSize(2);
        for (Permanent warden : wardens) {
            // 3/3 base + 0/1 from the other Warden = 3/4
            assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
        }
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Gallows Warden leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player1, new GhostWarden());

        Permanent ghostWarden = findPermanent(player1, "Ghost Warden");

        // Buffed: 1/1 + 0/1 = 1/2
        assertThat(gqs.getEffectiveToughness(gd, ghostWarden)).isEqualTo(2);

        // Remove Gallows Warden
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gallows Warden"));

        // Back to base 1/1
        assertThat(gqs.getEffectivePower(gd, ghostWarden)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ghostWarden)).isEqualTo(1);
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
