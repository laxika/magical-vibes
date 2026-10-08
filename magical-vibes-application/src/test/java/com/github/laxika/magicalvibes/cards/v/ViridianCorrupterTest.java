package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PeaceStrider;
import com.github.laxika.magicalvibes.cards.r.RotWolf;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianCorrupter.class, PeaceStrider.class, RotWolf.class})
class ViridianCorrupterTest extends BaseCardTest {

    @Test
    void castingDoesNotRequireAnEtbTarget() {
        harness.addToBattlefield(player2, new PeaceStrider());
        prepareCorrupter();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvingEntersBattlefieldAndChoosesEtbTarget() {
        harness.addToBattlefield(player2, new PeaceStrider());
        UUID targetId = harness.getPermanentId(player2, "Peace Strider");
        prepareCorrupter();
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viridian Corrupter");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, targetId);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new PeaceStrider());
        castAndChooseArtifact(harness.getPermanentId(player2, "Peace Strider"));

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Peace Strider");
        harness.assertInGraveyard(player2, "Peace Strider");
    }

    @Test
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new PeaceStrider());
        castAndChooseArtifact(harness.getPermanentId(player1, "Peace Strider"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Peace Strider");
        harness.assertInGraveyard(player1, "Peace Strider");
    }

    @Test
    void etbDoesNotResolveIfTargetRemoved() {
        harness.addToBattlefield(player2, new PeaceStrider());
        castAndChooseArtifact(harness.getPermanentId(player2, "Peace Strider"));
        gd.playerBattlefields.get(player2.getId()).clear();

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertOnBattlefield(player1, "Viridian Corrupter");
    }

    @Test
    void cannotChooseNonArtifactCreatureForEtb() {
        harness.addToBattlefield(player2, new RotWolf());
        harness.addToBattlefield(player2, new PeaceStrider());
        prepareCorrupter();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player2, "Rot Wolf")))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Peace Strider"));
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Rot Wolf");
        harness.assertInGraveyard(player2, "Peace Strider");
    }

    @Test
    void canCastWithoutTargetWhenNoArtifacts() {
        prepareCorrupter();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void etbLeavesNoAbilityOnStackWithoutLegalTargets() {
        prepareCorrupter();
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viridian Corrupter");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void artifactEnteringAfterCastingCanBeChosen() {
        prepareCorrupter();
        harness.castCreature(player1, 0);
        harness.addToBattlefield(player2, new PeaceStrider());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Peace Strider"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Peace Strider");
        harness.assertOnBattlefield(player1, "Viridian Corrupter");
    }

    @Test
    void etbResolvesAfterCorrupterLeavesBattlefield() {
        harness.addToBattlefield(player2, new PeaceStrider());
        castAndChooseArtifact(harness.getPermanentId(player2, "Peace Strider"));
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Peace Strider");
    }

    @Test
    void unblockedCombatDealsPoisonInsteadOfLifeLoss() {
        harness.setLife(player2, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ViridianCorrupter());
        attacker.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void combatDamageToCreaturePlacesMinusOneCounters() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PeaceStrider());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ViridianCorrupter());
        attacker.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Peace Strider");
        harness.assertInGraveyard(player1, "Viridian Corrupter");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new PeaceStrider());
        harness.setHand(player1, List.of(new ViridianCorrupter()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareCorrupter() {
        harness.setHand(player1, List.of(new ViridianCorrupter()));
        harness.addMana(player1, ManaColor.GREEN, 3);
    }

    private void castAndChooseArtifact(UUID targetId) {
        prepareCorrupter();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
    }
}
