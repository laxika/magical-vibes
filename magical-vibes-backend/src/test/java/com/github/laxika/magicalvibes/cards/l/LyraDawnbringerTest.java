package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LyraDawnbringerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Lyra Dawnbringer has static boost effect for Angels with lifelink")
    void hasCorrectStaticEffect() {
        LyraDawnbringer card = new LyraDawnbringer();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.LIFELINK);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    // ===== Static effect: buffs other Angels you control =====

    @Test
    @DisplayName("Other Angel creatures you control get +1/+1 and lifelink")
    void buffsOtherAngelsYouControl() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent serra = findPermanent(player1, "Serra Angel");

        // Serra Angel is 4/4 base + 1/1 from Lyra = 5/5
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lyra Dawnbringer does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new LyraDawnbringer());

        Permanent lyra = findPermanent(player1, "Lyra Dawnbringer");

        // Lyra is 5/5 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, lyra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lyra)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff non-Angel creatures")
    void doesNotBuffNonAngels() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Angel creatures")
    void doesNotBuffOpponentAngels() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent opponentAngel = findPermanent(player2, "Serra Angel");

        assertThat(gqs.getEffectivePower(gd, opponentAngel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentAngel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponentAngel, Keyword.LIFELINK)).isFalse();
    }

    // ===== Multiple Lyras =====

    @Test
    @DisplayName("Two Lyra Dawnbringers buff each other")
    void twoLyrasBuffEachOther() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new LyraDawnbringer());

        List<Permanent> lyras = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lyra Dawnbringer"))
                .toList();

        assertThat(lyras).hasSize(2);
        for (Permanent lyra : lyras) {
            assertThat(gqs.getEffectivePower(gd, lyra)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, lyra)).isEqualTo(6);
            assertThat(gqs.hasKeyword(gd, lyra, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Lyras give +2/+2 and lifelink to other Angels")
    void twoLyrasStackBonuses() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent serra = findPermanent(player1, "Serra Angel");

        // 4/4 base + 2/2 from two Lyras = 6/6
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Lyra Dawnbringer leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent serra = findPermanent(player1, "Serra Angel");

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lyra Dawnbringer"));

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Lyra Dawnbringer resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent serra = findPermanent(player1, "Serra Angel");
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isFalse();

        harness.setHand(player1, List.of(new LyraDawnbringer()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new SerraAngel());

        Permanent serra = findPermanent(player1, "Serra Angel");

        serra.setPowerModifier(serra.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(10); // 4 base + 5 spell + 1 static

        serra.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5); // 4 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
