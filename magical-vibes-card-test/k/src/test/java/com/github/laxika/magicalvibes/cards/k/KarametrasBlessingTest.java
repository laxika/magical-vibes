package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.l.LeoninOfTheLostPride;
import com.github.laxika.magicalvibes.cards.s.SentinelsEyes;
import com.github.laxika.magicalvibes.cards.s.ShatterTheSky;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarametrasBlessing.class, LeoninOfTheLostPride.class, SentinelsEyes.class,
        AlseidOfLifesBounty.class, BronzeSword.class, ShatterTheSky.class})
class KarametrasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2")
    void boostsRegularCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());

        castResolve(bear);

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An enchanted creature also gains hexproof and indestructible")
    void protectsEnchantedCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(bear.getId());

        castResolve(bear);

        assertProtectedAndBoosted(bear);
    }

    @Test
    @DisplayName("An enchantment creature also gains hexproof and indestructible")
    void protectsEnchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlseidOfLifesBounty());

        castResolve(creature);

        assertProtectedAndBoosted(creature);
    }

    @Test
    @DisplayName("The boost and granted keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());

        castResolve(bear);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Granted protection expires at cleanup on an enchantment creature")
    void protectionExpiresAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlseidOfLifesBounty());
        castResolve(creature);
        assertProtectedAndBoosted(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Equipment alone does not make a creature enchanted")
    void equipmentDoesNotGrantProtection() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        equipment.setAttachedTo(creature.getId());

        castResolve(creature);

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature can receive the boost and protection")
    void canProtectOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlseidOfLifesBounty());

        castResolve(creature);

        assertProtectedAndBoosted(creature);
    }

    @Test
    @DisplayName("An opponent-controlled Aura qualifies the creature as enchanted")
    void opponentsAuraGrantsProtection() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());

        castResolve(creature);

        assertProtectedAndBoosted(creature);
    }

    @Test
    @DisplayName("Losing the Aura before resolution still grants the boost but no protection")
    void auraRemovedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Becoming enchanted before resolution grants protection")
    void auraAddedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());

        harness.passBothPriorities();

        assertProtectedAndBoosted(creature);
    }

    @Test
    @DisplayName("Losing the Aura after resolution does not remove granted protection")
    void auraRemovedAfterResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());
        castResolve(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.runStateBasedActions();

        assertProtectedAndBoosted(creature);
    }

    @Test
    @DisplayName("Becoming enchanted after resolution does not grant protection retroactively")
    void auraAddedAfterResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeoninOfTheLostPride());
        castResolve(creature);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());
        harness.runStateBasedActions();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Granted hexproof prevents opponents from targeting the creature")
    void hexproofRejectsOpponentsSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlseidOfLifesBounty());
        castResolve(creature);
        harness.setHand(player2, List.of(new KarametrasBlessing()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted indestructible protects the creature from mass destruction")
    void survivesMassDestruction() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new AlseidOfLifesBounty());
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player2, new AlseidOfLifesBounty());
        castResolve(protectedCreature);
        harness.setHand(player1, List.of(new ShatterTheSky()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(unprotectedCreature);
        harness.assertInGraveyard(player2, "Alseid of Life's Bounty");
    }

    private void assertProtectedAndBoosted(Permanent creature) {
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
