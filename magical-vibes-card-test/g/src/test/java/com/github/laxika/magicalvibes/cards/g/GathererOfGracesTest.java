package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DebtorsKnell;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.s.SilhanaLedgewalker;
import com.github.laxika.magicalvibes.cards.s.SinstrikersWill;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GathererOfGraces.class, DebtorsKnell.class, Gristleback.class,
        SilhanaLedgewalker.class, SinstrikersWill.class})
class GathererOfGracesTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Aura attached to it")
    void getsBoostForEachAttachedAura() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());

        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        firstAura.setAttachedTo(gatherer.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        secondAura.setAttachedTo(gatherer.getId());
        Permanent auraAttachedElsewhere = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        auraAttachedElsewhere.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, gatherer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gatherer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing an Aura regenerates Gatherer of Graces")
    void sacrificingAuraRegeneratesGatherer() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        aura.setAttachedTo(gatherer.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gatherer.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, gatherer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gatherer)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Sinstriker's Will");
    }

    @Test
    @DisplayName("Regeneration shield saves Gatherer of Graces from lethal combat damage")
    void regenerationSavesGathererFromLethalCombatDamage() {
        Permanent gatherer = addCreatureReady(player1, new GathererOfGraces());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        aura.setAttachedTo(gatherer.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gatherer.setBlocking(true);
        gatherer.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new Gristleback());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Gatherer of Graces");
        Permanent survivor = findPermanent(player1, "Gatherer of Graces");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
        assertThat(survivor.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Sinstriker's Will");
    }

    @Test
    @DisplayName("A non-Aura enchantment cannot pay the regeneration cost")
    void nonAuraCannotPayRegenerationCost() {
        harness.addToBattlefield(player1, new GathererOfGraces());
        harness.addToBattlefield(player1, new DebtorsKnell());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent-controlled Auras attached to Gatherer also grant its bonus")
    void opponentControlledAuraGrantsBonus() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SinstrikersWill());
        aura.setAttachedTo(gatherer.getId());

        assertThat(gqs.getEffectivePower(gd, gatherer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gatherer)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent-controlled Aura cannot pay the cost even when attached to Gatherer")
    void cannotSacrificeOpponentControlledAura() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SinstrikersWill());
        aura.setAttachedTo(gatherer.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Sinstriker's Will");
        assertThat(gatherer.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Aura attached elsewhere is sacrificed as a cost before Gatherer regenerates")
    void sacrificesAuraAttachedElsewhereBeforeResolution() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        aura.setAttachedTo(otherCreature.getId());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Sinstriker's Will");
        harness.assertNotOnBattlefield(player1, "Sinstriker's Will");
        assertThat(gd.stack).hasSize(1);
        assertThat(gatherer.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(gatherer.getRegenerationShield()).isEqualTo(1);
        assertThat(otherCreature.getRegenerationShield()).isZero();
        assertThat(gatherer.isTapped()).isFalse();
    }
}
