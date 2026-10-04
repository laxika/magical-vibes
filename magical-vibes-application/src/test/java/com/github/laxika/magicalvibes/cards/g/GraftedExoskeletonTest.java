package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.r.RustedRelic;
import com.github.laxika.magicalvibes.cards.s.SylvokReplica;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraftedExoskeleton.class, GrizzlyBears.class, Naturalize.class,
        CarapaceForger.class, RustedRelic.class, SylvokReplica.class})
class GraftedExoskeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires two mana")
    void equipRequiresTwoMana() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(exoskeleton.isAttached()).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(exoskeleton.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Casting Grafted Exoskeleton puts it on the battlefield unattached")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GraftedExoskeleton()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grafted Exoskeleton")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Exoskeleton to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(exoskeleton.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Equipped creature has infect")
    void equippedCreatureHasInfect() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Creature loses infect when Exoskeleton is removed")
    void creatureLosesInfectWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(exoskeleton);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature deals combat damage as poison counters to defending player")
    void infectDealsPoisonCountersToPlayer() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 4 power (2 + 2), infect deals poison counters
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        // Infect damage does not cause loss of life.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Equipped creature deals combat damage as -1/-1 counters to blocking creature")
    void infectDealsMinusCountersToCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 2/2 blocker
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Attacker has 4 power with infect → puts 4 -1/-1 counters on blocker
        // Blocker was 2/2, now -2/-2 toughness → dead (SBA)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Re-equipping Grafted Exoskeleton sacrifices the previously equipped creature")
    void reEquipSacrificesPreviousCreature() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        // Attach to creature1 first
        exoskeleton.setAttachedTo(creature1.getId());

        // Re-equip to creature2
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities(); // Equip resolves.
        harness.passBothPriorities(); // The unattachment trigger resolves.

        // Exoskeleton now on creature2
        assertThat(exoskeleton.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 was sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        // creature1 went to graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // creature2 is still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature2.getId()));
    }

    @Test
    @DisplayName("Destroying Grafted Exoskeleton sacrifices the equipped creature")
    void destroyingExoskeletonSacrificesCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        exoskeleton.setAttachedTo(creature.getId());

        // Player1 casts something so player2 can respond with Naturalize
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, exoskeleton.getId());
        harness.passBothPriorities(); // Naturalize resolves
        harness.passBothPriorities(); // The unattachment trigger resolves.
        harness.passBothPriorities(); // Grizzly Bears resolves

        // Exoskeleton is destroyed
        harness.assertNotOnBattlefield(player1, "Grafted Exoskeleton");
        // Creature is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        // Both went to graveyard
        harness.assertInGraveyard(player1, "Grafted Exoskeleton");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipping from unattached state does not sacrifice anything")
    void equippingFromUnattachedDoesNotSacrifice() {
        harness.addToBattlefield(player1, new GraftedExoskeleton());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        // Exoskeleton is unattached, equip to creature1
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature1.getId());
        harness.passBothPriorities();

        // Both creatures still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature1.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature2.getId()));
        // No creatures in graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Re-equipping to same creature does not sacrifice it")
    void reEquipToSameCreatureDoesNotSacrifice() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        exoskeleton.setAttachedTo(creature.getId());

        // Re-equip to same creature
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Creature is still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Re-equipping puts the sacrifice trigger on the stack before sacrificing")
    void reEquipAllowsResponsesBeforeSacrifice() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent oldHost = addCreatureReady(player1, new CarapaceForger());
        Permanent newHost = addCreatureReady(player1, new CarapaceForger());
        exoskeleton.setAttachedTo(oldHost.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, newHost.getId());
        harness.passBothPriorities();

        assertThat(exoskeleton.getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oldHost);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, oldHost)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oldHost, Keyword.INFECT)).isFalse();

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oldHost);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(newHost);
    }

    @Test
    @DisplayName("Unattachment cannot sacrifice a permanent controlled by an opponent")
    void reEquipCannotSacrificeOpponentsCreature() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent oldHost = addCreatureReady(player2, new CarapaceForger());
        Permanent newHost = addCreatureReady(player1, new CarapaceForger());
        exoskeleton.setAttachedTo(oldHost.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, newHost.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(exoskeleton.getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(oldHost);
    }

    @Test
    @DisplayName("Losing creature status triggers sacrifice of the former host")
    void losingCreatureStatusTriggersSacrifice() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new SylvokReplica());
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        exoskeleton.setAttachedTo(relic.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 2, null, exoskeleton.getId());

        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(exoskeleton.isAttached()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(relic);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rusted Relic");
        harness.assertInGraveyard(player1, "Rusted Relic");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grafted Exoskeleton");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(exoskeleton.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated with a spell on the stack")
    void equipRequiresEmptyStack() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new CarapaceForger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(exoskeleton.isAttached()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Equipment destruction leaves its host alive until the sacrifice trigger resolves")
    void destructionAllowsResponsesBeforeSacrifice() {
        Permanent exoskeleton = harness.addToBattlefieldAndReturn(player1, new GraftedExoskeleton());
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        exoskeleton.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new SylvokReplica());
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, exoskeleton.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grafted Exoskeleton");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        harness.assertInGraveyard(player1, "Carapace Forger");
    }
}
