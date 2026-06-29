package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroySubtypeCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoodenStakeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has +1/+0 static boost for equipped creature")
    void hasStaticBoostEffect() {
        WoodenStake card = new WoodenStake();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Has DestroySubtypeCombatOpponentEffect on ON_BLOCK and ON_BECOMES_BLOCKED")
    void hasCombatDestroyEffects() {
        WoodenStake card = new WoodenStake();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).singleElement()
                .isInstanceOf(DestroySubtypeCombatOpponentEffect.class);
        DestroySubtypeCombatOpponentEffect blockEffect =
                (DestroySubtypeCombatOpponentEffect) card.getEffects(EffectSlot.ON_BLOCK).getFirst();
        assertThat(blockEffect.requiredSubtype()).isEqualTo(CardSubtype.VAMPIRE);
        assertThat(blockEffect.cannotBeRegenerated()).isTrue();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).singleElement()
                .isInstanceOf(DestroySubtypeCombatOpponentEffect.class);
        assertThat(card.getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED).getFirst().triggerMode())
                .isEqualTo(TriggerMode.PER_BLOCKER);
    }

    // ===== Equipped creature blocks a Vampire =====

    @Test
    @DisplayName("When equipped creature blocks a Vampire, a trigger is created to destroy it")
    void blockingVampireCreatesTrigger() {
        Permanent creature = addReadyCreature(player2);
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());

        Permanent vampire = addReadyVampire(player1);
        vampire.setAttacking(true);

        setupDeclareBlockers();
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
        Permanent creature = addReadyCreature(player2);
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());

        Permanent vampire = addReadyVampire(player1);
        vampire.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Vampire destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vampire Aristocrat"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vampire Aristocrat"));

        // Equipped creature still alive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("When equipped creature blocks a non-Vampire, no trigger is created")
    void blockingNonVampireNoTrigger() {
        Permanent creature = addReadyCreature(player2);
        Permanent stake = addStake(player2);
        stake.setAttachedTo(creature.getId());

        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isZero();
    }

    // ===== Equipped creature becomes blocked by a Vampire =====

    @Test
    @DisplayName("When equipped creature becomes blocked by a Vampire, a trigger is created to destroy it")
    void becomingBlockedByVampireCreatesTrigger() {
        Permanent creature = addReadyCreature(player1);
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent vampire = addReadyVampire(player2);

        setupDeclareBlockers();
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
        Permanent creature = addReadyCreature(player1);
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent vampire = addReadyVampire(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Vampire destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vampire Aristocrat"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Vampire Aristocrat"));
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by a non-Vampire, no trigger is created")
    void becomingBlockedByNonVampireNoTrigger() {
        Permanent creature = addReadyCreature(player1);
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isZero();
    }

    // ===== Mixed blockers =====

    @Test
    @DisplayName("When equipped creature becomes blocked by Vampire and non-Vampire, trigger only for Vampire")
    void mixedBlockersTriggerOnlyForVampire() {
        Permanent creature = addReadyCreature(player1);
        Permanent stake = addStake(player1);
        stake.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent vampire = addReadyVampire(player2);
        addReadyCreature(player2);

        setupDeclareBlockers();
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

    // ===== No trigger when not equipped =====

    @Test
    @DisplayName("No trigger when Wooden Stake is not attached to any creature")
    void noTriggerWhenNotEquipped() {
        addReadyCreature(player2);
        addStake(player2); // On battlefield but not attached

        Permanent vampire = addReadyVampire(player1);
        vampire.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long stakeTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Wooden Stake"))
                .count();
        assertThat(stakeTriggers).isZero();
    }

    // ===== Helpers =====

    private Permanent addStake(Player player) {
        Permanent perm = new Permanent(new WoodenStake());
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

    /**
     * VampireAristocrat (3/2 Vampire) — no evasion, can attack and block.
     */
    private Permanent addReadyVampire(Player player) {
        Permanent perm = new Permanent(new VampireAristocrat());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
