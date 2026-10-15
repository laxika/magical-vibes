package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MarkovPatrician;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodenStake.class, WalkingCorpse.class, MarkovPatrician.class})
class WoodenStakeTest extends BaseCardTest {

    @Test
    @DisplayName("When equipped creature blocks a Vampire, a trigger is created to destroy it")
    void blockingVampireCreatesTrigger() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());

        Permanent vampire = addReadyVampire(player1);
        vampire.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Wooden Stake")
                        && se.getTargetId().equals(vampire.getId())
                        && se.getSourcePermanentId().equals(stake.getId()));
    }

    @Test
    @DisplayName("When equipped creature blocks a Vampire, resolving the trigger destroys the Vampire")
    void blockingVampireDestroysIt() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());

        Permanent vampire = addReadyVampire(player1);
        vampire.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Vampire destroyed
        harness.assertNotOnBattlefield(player1, "Markov Patrician");
        harness.assertInGraveyard(player1, "Markov Patrician");

        // Equipped creature still alive
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("When equipped creature blocks a non-Vampire, no trigger is created")
    void blockingNonVampireNoTrigger() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());

        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isZero();
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by a Vampire, a trigger is created to destroy it")
    void becomingBlockedByVampireCreatesTrigger() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent vampire = addReadyVampire(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Wooden Stake")
                        && se.getTargetId().equals(vampire.getId())
                        && se.getSourcePermanentId().equals(stake.getId()));
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by a Vampire, resolving the trigger destroys it")
    void becomingBlockedByVampireDestroysIt() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent vampire = addReadyVampire(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Vampire destroyed
        harness.assertNotOnBattlefield(player2, "Markov Patrician");
        harness.assertInGraveyard(player2, "Markov Patrician");
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by a non-Vampire, no trigger is created")
    void becomingBlockedByNonVampireNoTrigger() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new WalkingCorpse());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isZero();
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by Vampire and non-Vampire, trigger only for Vampire")
    void mixedBlockersTriggerOnlyForVampire() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent vampire = addReadyVampire(player2);
        addCreatureReady(player2, new WalkingCorpse());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isEqualTo(1);
        assertThat(gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .findFirst().get().getTargetId()).isEqualTo(vampire.getId());
    }

    @Test
    @DisplayName("No trigger when Wooden Stake is not attached to any creature")
    void noTriggerWhenNotEquipped() {
        addCreatureReady(player2, new WalkingCorpse());
        addStake(player2); // On battlefield but not attached

        Permanent vampire = addReadyVampire(player1);
        vampire.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isZero();
    }

    @Test
    @DisplayName("Equip moves the power bonus to the new creature without changing toughness")
    void equipMovesPowerBonus() {
        Permanent stake = addStake(player1);
        Permanent first = addCreatureReady(player1, new WalkingCorpse());
        Permanent second = addCreatureReady(player1, new WalkingCorpse());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(stake.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(stake.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Vampire blocker is destroyed by its own trigger")
    void multipleVampireBlockersAreDestroyed() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent first = addReadyVampire(player2);
        Permanent second = addReadyVampire(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Wooden Stake"))
                .map(entry -> entry.getTargetId()))
                .containsExactlyInAnyOrder(first.getId(), second.getId());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(countPermanents(player1, "Walking Corpse")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Markov Patrician")).hasSize(2);
    }

    @Test
    @DisplayName("A regeneration shield cannot save the Vampire")
    void vampireCannotRegenerate() {
        Permanent creature = addCreatureReady(player2, new WalkingCorpse());
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());
        Permanent vampire = addReadyVampire(player1);
        vampire.setRegenerationShield(1);
        vampire.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Markov Patrician");
        harness.assertInGraveyard(player1, "Markov Patrician");
    }

    @Test
    @DisplayName("Detaching Wooden Stake after the trigger does not save the Vampire")
    void triggerResolvesAfterStakeDetaches() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        addReadyVampire(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        stake.setAttachedTo(null);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Markov Patrician");
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Wooden Stake's controller controls its trigger even when an opponent controls the equipped creature")
    void stakeControllerControlsBecomesBlockedTrigger() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent vampire = addReadyVampire(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack)
                .filteredOn(entry -> entry.getCard().getName().equals("Wooden Stake"))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getControllerId()).isEqualTo(player2.getId());
                    assertThat(entry.getTargetId()).isEqualTo(vampire.getId());
                });
    }

    private Permanent addStake(Player player) {
        return harness.addToBattlefieldAndReturn(player, new WoodenStake());
    }

    private Permanent addReadyVampire(Player player) {
        return addCreatureReady(player, new MarkovPatrician());
    }
}
