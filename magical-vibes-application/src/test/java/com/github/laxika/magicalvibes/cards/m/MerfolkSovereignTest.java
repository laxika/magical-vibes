package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerfolkSovereignTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Merfolk Sovereign has static boost effect for Merfolk")
    void hasStaticBoostEffect() {
        MerfolkSovereign card = new MerfolkSovereign();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Merfolk Sovereign has tap activated ability with MakeCreatureUnblockableEffect")
    void hasUnblockableActivatedAbility() {
        MerfolkSovereign card = new MerfolkSovereign();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MakeCreatureUnblockableEffect.class);
    }

    // ===== Static effect: buffs other Merfolk you control =====

    @Test
    @DisplayName("Other Merfolk creatures you control get +1/+1")
    void buffsOtherMerfolk() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent otherMerfolk = addMerfolkReady(player1);

        assertThat(gqs.getEffectivePower(gd, otherMerfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherMerfolk)).isEqualTo(3);
    }

    @Test
    @DisplayName("Merfolk Sovereign does not buff itself")
    void doesNotBuffItself() {
        Permanent sovereign = addSovereignReady(player1);

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        addSovereignReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Merfolk creatures")
    void doesNotBuffOpponentMerfolk() {
        addSovereignReady(player1);
        Permanent opponentMerfolk = addMerfolkReady(player2);

        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Merfolk Sovereigns buff each other")
    void twoSovereignsBuffEachOther() {
        Permanent sovereign1 = addSovereignReady(player1);
        Permanent sovereign2 = addSovereignReady(player1);

        assertThat(gqs.getEffectivePower(gd, sovereign1)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sovereign1)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sovereign2)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sovereign2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Merfolk Sovereign leaves the battlefield")
    void bonusRemovedWhenSovereignLeaves() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent otherMerfolk = addMerfolkReady(player1);

        assertThat(gqs.getEffectivePower(gd, otherMerfolk)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(sovereign);

        assertThat(gqs.getEffectivePower(gd, otherMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherMerfolk)).isEqualTo(2);
    }

    // ===== Activated ability: make target Merfolk unblockable =====

    @Test
    @DisplayName("Activating ability makes target Merfolk creature unblockable this turn")
    void activatingAbilityMakesMerfolkUnblockable() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent targetMerfolk = addMerfolkReady(player1);

        harness.activateAbility(player1, 0, null, targetMerfolk.getId());
        harness.passBothPriorities();

        assertThat(targetMerfolk.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps the Sovereign")
    void activatingAbilityTapsSovereign() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent targetMerfolk = addMerfolkReady(player1);

        harness.activateAbility(player1, 0, null, targetMerfolk.getId());

        assertThat(sovereign.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn")
    void unblockableResetsAtEndOfTurn() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent targetMerfolk = addMerfolkReady(player1);

        harness.activateAbility(player1, 0, null, targetMerfolk.getId());
        harness.passBothPriorities();

        assertThat(targetMerfolk.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(targetMerfolk.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target opponent's Merfolk creature")
    void canTargetOpponentMerfolk() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent opponentMerfolk = addMerfolkReady(player2);

        harness.activateAbility(player1, 0, null, opponentMerfolk.getId());
        harness.passBothPriorities();

        assertThat(opponentMerfolk.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target non-Merfolk creature with activated ability")
    void cannotTargetNonMerfolk() {
        Permanent sovereign = addSovereignReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target Merfolk is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        Permanent sovereign = addSovereignReady(player1);
        Permanent targetMerfolk = addMerfolkReady(player1);

        harness.activateAbility(player1, 0, null, targetMerfolk.getId());

        gd.playerBattlefields.get(player1.getId()).remove(targetMerfolk);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSovereignReady(Player player) {
        Permanent perm = new Permanent(new MerfolkSovereign());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addMerfolkReady(Player player) {
        Permanent perm = new Permanent(new MerfolkSovereign());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
