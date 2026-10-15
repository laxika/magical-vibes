package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaggerOfTheWorthy.class, FeralProwler.class, Humble.class})
class DaggerOfTheWorthyTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        Permanent dagger = addDagger(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {2} attaches to target creature")
    void equipAttaches() {
        Permanent dagger = addDagger(player1);
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Afflict 1: equipped creature becoming blocked makes defending player lose 1 life")
    void blockedAfflictsDefender() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        Permanent dagger = addDagger(player1);
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        addCreatureReady(player2, new FeralProwler());

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Afflict does not trigger when unequipped creature is blocked")
    void unequippedDoesNotAfflict() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        addDagger(player1); // on battlefield but not attached
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        addCreatureReady(player2, new FeralProwler());

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void multipleBlockersCauseOnlyOneLifeLoss() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        addDagger(player1).setAttachedTo(creature.getId());
        addCreatureReady(player2, new FeralProwler());
        addCreatureReady(player2, new FeralProwler());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void twoDaggersGrantTwoSeparateAfflictAbilities() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        addDagger(player1).setAttachedTo(creature.getId());
        addDagger(player1).setAttachedTo(creature.getId());
        addCreatureReady(player2, new FeralProwler());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void losingAbilitiesAfterEquippingRemovesAfflict() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        addDagger(player1).setAttachedTo(creature.getId());
        addCreatureReady(player2, new FeralProwler());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void detachingDaggerRemovesPowerBoost() {
        Permanent creature = addCreatureReady(player1, new FeralProwler());
        Permanent dagger = addDagger(player1);
        dagger.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        dagger.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
    }

    private Permanent addDagger(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DaggerOfTheWorthy());
    }
}

