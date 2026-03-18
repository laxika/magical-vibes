package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilverInlaidDaggerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Silver-Inlaid Dagger has unconditional +2/+0 static boost")
    void hasUnconditionalBoostEffect() {
        SilverInlaidDagger card = new SilverInlaidDagger();

        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect sbe && sbe.filter() == null)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Silver-Inlaid Dagger has conditional +1/+0 boost for Humans")
    void hasConditionalHumanBoostEffect() {
        SilverInlaidDagger card = new SilverInlaidDagger();

        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect sbe && sbe.filter() != null)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
        assertThat(boost.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);
        assertThat(((PermanentHasSubtypePredicate) boost.filter()).subtype()).isEqualTo(CardSubtype.HUMAN);
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped non-Human creature gets +2/+0")
    void equippedNonHumanGetsBaseBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped Human creature gets +3/+0 (base +2 plus Human +1)")
    void equippedHumanGetsFullBoost() {
        Permanent human = addReadyHuman(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);   // 2 + 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1); // 1 + 0
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Equipped Human deals 5 combat damage (2 base + 2 + 1)")
    void equippedHumanDealsCombatDamage() {
        harness.setLife(player2, 20);

        Permanent human = addReadyHuman(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(human.getId());
        human.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15); // 20 - 5
    }

    @Test
    @DisplayName("Equipped non-Human deals 4 combat damage (2 base + 2)")
    void equippedNonHumanDealsCombatDamage() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 20 - 4
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving Dagger from Human to non-Human removes the Human bonus")
    void movingFromHumanToNonHumanRemovesBonus() {
        Permanent dagger = addDaggerReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent creature = addReadyCreature(player1);
        dagger.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5); // 2 + 2 + 1

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);    // back to base
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4); // 2 + 2 (no Human bonus)
    }

    @Test
    @DisplayName("Moving Dagger from non-Human to Human grants the Human bonus")
    void movingFromNonHumanToHumanGrantsBonus() {
        Permanent dagger = addDaggerReady(player1);
        Permanent creature = addReadyCreature(player1);
        Permanent human = addReadyHuman(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4); // 2 + 2

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5); // 2 + 2 + 1
    }

    // ===== Helpers =====

    private Permanent addDaggerReady(Player player) {
        Permanent perm = new Permanent(new SilverInlaidDagger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyHuman(Player player) {
        Permanent perm = new Permanent(new EliteVanguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
