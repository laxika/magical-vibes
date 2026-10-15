package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeltstridersResolve.class, GrizzlyBears.class, Disenchant.class})
class MeltstridersResolveTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature gets +0/+2 before fighting an opposing creature")
    void boostsBeforeFight() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAura(creature);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Aura enters without a fight target when none exists")
    void resolvesWithoutOpposingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castAura(creature);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature can't be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAura(attacker);
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        List<Permanent> blockers = gd.playerBattlefields.get(player2.getId());
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockers.indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(blockers.indexOf(blockerTwo), attackerIndex)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("The enchanted creature can still be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castAura(attacker);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex)
        ));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The Aura can enchant only a creature its controller controls")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MeltstridersResolve()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The controller can decline to fight even when an opposing creature exists")
    void canDeclineFight() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAura(creature);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The fight still happens if the Aura is destroyed in response to its trigger")
    void fightsAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAura(creature);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        Permanent aura = findPermanent(player1, "Meltstrider's Resolve");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Meltstrider's Resolve");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Neither creature fights if the chosen opponent creature leaves before resolution")
    void doesNotFightMissingTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAura(creature);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opponentCreature));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Meltstrider's Resolve");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The fight trigger cannot target a creature controlled by the Aura's controller")
    void cannotFightOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castAura(creature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opposing creature takes no fight damage if the enchanted creature leaves")
    void doesNotFightMissingEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castAura(creature);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Meltstrider's Resolve");
    }

    private void castAura(Permanent creature) {
        harness.setHand(player1, List.of(new MeltstridersResolve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
