package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ButchersCleaverTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Butcher's Cleaver has static +3/+0 boost effect")
    void hasStaticBoostEffect() {
        ButchersCleaver card = new ButchersCleaver();

        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Butcher's Cleaver grants lifelink only to equipped Human creatures")
    void hasConditionalLifelinkEffect() {
        ButchersCleaver card = new ButchersCleaver();

        GrantKeywordEffect grant = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .findFirst().orElseThrow();
        assertThat(grant.keyword()).isEqualTo(Keyword.LIFELINK);
        assertThat(grant.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
        assertThat(grant.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);
        assertThat(((PermanentHasSubtypePredicate) grant.filter()).subtype()).isEqualTo(CardSubtype.HUMAN);
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +3/+0 regardless of creature type")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped Human creature gets +3/+0")
    void equippedHumanGetsBoost() {
        Permanent human = addReadyHuman(player1);
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1); // 1 + 0
    }

    // ===== Static effects: conditional lifelink =====

    @Test
    @DisplayName("Equipped Human creature has lifelink")
    void equippedHumanHasLifelink() {
        Permanent human = addReadyHuman(player1);
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Equipped non-Human creature does not have lifelink")
    void equippedNonHumanDoesNotHaveLifelink() {
        Permanent creature = addReadyCreature(player1);
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    // ===== Lifelink in combat =====

    @Test
    @DisplayName("Controller gains life when equipped Human deals combat damage")
    void lifelinkGainsLifeOnHumanCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent human = addReadyHuman(player1);
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(human.getId());
        human.setAttacking(true);

        resolveCombat();

        // Human has 5 power (2 + 3), player2 takes 5: 20 - 5 = 15
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Player1 gains 5 from lifelink: 20 + 5 = 25
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Controller does not gain life when equipped non-Human deals combat damage")
    void noLifelinkOnNonHumanCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 5 power (2 + 3), player2 takes 5: 20 - 5 = 15
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Player1 does NOT gain life (no lifelink): stays at 20
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Moving Cleaver from Human to non-Human removes lifelink")
    void movingFromHumanToNonHumanRemovesLifelink() {
        Permanent cleaver = addCleaverReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent creature = addReadyCreature(player1);
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Moving Cleaver from non-Human to Human grants lifelink")
    void movingFromNonHumanToHumanGrantsLifelink() {
        Permanent cleaver = addCleaverReady(player1);
        Permanent creature = addReadyCreature(player1);
        Permanent human = addReadyHuman(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);
    }

    // ===== Helpers =====

    private Permanent addCleaverReady(Player player) {
        Permanent perm = new Permanent(new ButchersCleaver());
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
