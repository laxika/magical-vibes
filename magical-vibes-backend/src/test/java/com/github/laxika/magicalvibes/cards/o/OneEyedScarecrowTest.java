package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OneEyedScarecrowTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("One-Eyed Scarecrow has a static boost effect targeting opponent flying creatures")
    void hasCorrectStaticEffect() {
        OneEyedScarecrow card = new OneEyedScarecrow();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(-1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
        assertThat(effect.scope()).isEqualTo(GrantScope.OPPONENT_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasKeywordPredicate.class);
    }

    // ===== Static effect: debuffs opponent's flying creatures =====

    @Test
    @DisplayName("Opponent's flying creature gets -1/-0")
    void debuffsOpponentFlyingCreature() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player2, new BattlegroundGeist());

        // BattlegroundGeist is 3/3 with flying; should become 2/3
        Permanent geist = findPermanent(player2, "Battleground Geist");

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's non-flying creature is not affected")
    void doesNotDebuffOpponentNonFlyingCreature() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        // 2/2 base, unaffected
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own flying creature is not affected")
    void doesNotDebuffOwnFlyingCreature() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player1, new BattlegroundGeist());

        Permanent geist = findPermanent(player1, "Battleground Geist");

        // 3/3 base, unaffected by own Scarecrow
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    // ===== Does not buff itself =====

    @Test
    @DisplayName("Does not affect itself")
    void doesNotAffectItself() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());

        Permanent scarecrow = findPermanent(player1, "One-Eyed Scarecrow");

        // 2/3 base, no self-debuff
        assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(3);
    }

    // ===== Defender keyword =====

    @Test
    @DisplayName("One-Eyed Scarecrow has defender")
    void hasDefender() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());

        Permanent scarecrow = findPermanent(player1, "One-Eyed Scarecrow");

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.DEFENDER)).isTrue();
    }

    // ===== Multiple Scarecrows stack =====

    @Test
    @DisplayName("Two Scarecrows give -2/-0 to opponent's flying creature")
    void twoScarecrowsStack() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player2, new BattlegroundGeist());

        Permanent geist = findPermanent(player2, "Battleground Geist");

        // 3/3 base - 2/0 from two Scarecrows = 1/3
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Debuff is removed when Scarecrow leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new OneEyedScarecrow());
        harness.addToBattlefield(player2, new BattlegroundGeist());

        Permanent geist = findPermanent(player2, "Battleground Geist");

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("One-Eyed Scarecrow"));

        // Back to base 3/3
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    // ===== Helper methods =====

}
