package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ClayGolem;
import com.github.laxika.magicalvibes.cards.d.DragonbornChampion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.cards.t.TaureanMauler;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WandOfOrcus.class, GrizzlyBears.class, ZombieCutthroat.class,
        ClayGolem.class, DragonbornChampion.class, MagneticTheft.class, TaureanMauler.class})
class WandOfOrcusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the equipped creature grants deathtouch to it and your Zombies")
    void attackGrantsDeathtouchToEquippedCreatureAndZombies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player1, new ZombieCutthroat());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Blocking with the equipped creature grants deathtouch to it and your Zombies")
    void blockGrantsDeathtouchToEquippedCreatureAndZombies() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player1, new ZombieCutthroat());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, 0)));
        resolveAllTriggers();

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Combat damage from the equipped creature creates that many Zombies")
    void combatDamageCreatesThatManyZombies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
        assertThat(findPermanents(player1, "Zombie")).allSatisfy(zombie -> {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    void equipCostsThreeManaAndAttachesToYourCreature() {
        Permanent creature = addCreatureReady(player1, new ClayGolem());
        Permanent wand = addWandReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wand.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, creature.getId());
        resolveAllTriggers();

        assertThat(wand.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void equipCannotTargetOpponentsCreatureOrBeActivatedDuringCombat() {
        Permanent ownCreature = addCreatureReady(player1, new ClayGolem());
        Permanent opposingCreature = addCreatureReady(player2, new ClayGolem());
        Permanent wand = addWandReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wand.getAttachedTo()).isNull();

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wand.getAttachedTo()).isNull();
    }

    @Test
    void unequippedAttackerDoesNotGrantDeathtouchOrCreateZombies() {
        Permanent attacker = addCreatureReady(player1, new ClayGolem());
        Permanent equippedCreature = addCreatureReady(player1, new ClayGolem());
        Permanent zombie = addCreatureReady(player1, new TaureanMauler());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(equippedCreature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isFalse();
        resolveCombat();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void damageToBlockingCreatureDoesNotCreateZombies() {
        Permanent attacker = addCreatureReady(player1, new ClayGolem());
        addCreatureReady(player2, new ClayGolem());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void attackTriggerStillGrantsDeathtouchToOriginalCreatureAfterWandMoves() {
        Permanent attacker = addCreatureReady(player1, new ClayGolem());
        Permanent otherCreature = addCreatureReady(player1, new ClayGolem());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, List.of(wand.getId(), otherCreature.getId()));
            resolveAllTriggers();
        });

        assertThat(wand.getAttachedTo()).isEqualTo(otherCreature.getId());
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void trampleCreatesZombiesOnlyForDamageDealtToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new DragonbornChampion());
        Permanent blocker = addCreatureReady(player2, new TaureanMauler());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 4));
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(findPermanents(player1, "Zombie")).hasSize(4);
    }

    @Test
    void zombiesEnteringAfterAttackTriggerResolvesDoNotGainDeathtouch() {
        Permanent attacker = addCreatureReady(player1, new ClayGolem());
        Permanent zombie = addCreatureReady(player1, new TaureanMauler());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).hasSize(4);
        assertThat(findPermanents(player1, "Zombie")).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isFalse());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void wandControllerGetsTokensAndGrantsDeathtouchOnlyToTheirZombies() {
        Permanent attacker = addCreatureReady(player1, new ClayGolem());
        Permanent attackersZombie = addCreatureReady(player1, new TaureanMauler());
        Permanent wandOwnersZombie = addCreatureReady(player2, new TaureanMauler());
        Permanent wand = addWandReady(player2);
        wand.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackersZombie, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, wandOwnersZombie, Keyword.DEATHTOUCH)).isTrue();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(findPermanents(player2, "Zombie")).hasSize(4);
    }

    private Permanent addWandReady(Player player) {
        return addCreatureReady(player, new WandOfOrcus());
    }
}
