package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DictateOfKarametra;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldenhideOx.class, PensiveMinotaur.class, DictateOfKarametra.class})
class GoldenhideOxTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry makes a target creature must be blocked")
    void ownEntryMakesTargetMustBeBlocked() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        castGoldenhideOx(player1, bears.getId());

        resolveAllTriggers();

        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers it")
    void allyEnchantmentEntryMakesTargetMustBeBlocked() {
        harness.addToBattlefield(player1, new GoldenhideOx());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());

        harness.setHand(player1, List.of(new DictateOfKarametra()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoldenhideOx());
        harness.setHand(player2, List.of(new DictateOfKarametra()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The effect wears off at the end of the turn")
    void mustBeBlockedWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        castGoldenhideOx(player1, bears.getId());

        resolveAllTriggers();
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Its entry cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new DictateOfKarametra());
        harness.setHand(player1, List.of(new GoldenhideOx()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its own entry triggers once and can target Goldenhide Ox itself")
    void ownEntryCanTargetItselfExactlyOnce() {
        harness.setHand(player1, List.of(new GoldenhideOx()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ox = findPermanent(player1, "Goldenhide Ox");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ox.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(ox.isMustBeBlockedThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Constellation does not transfer to another creature if its target leaves")
    void removedTargetDoesNotAffectOtherCreatures() {
        harness.addToBattlefield(player1, new GoldenhideOx());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new DictateOfKarametra()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(other.isMustBeBlockedThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The required creature cannot be left unblocked when a blocker is available")
    void availableBlockerMustBlockTarget() {
        Permanent target = addCreatureReady(player1, new PensiveMinotaur());
        addCreatureReady(player2, new PensiveMinotaur());
        castGoldenhideOx(player1, target.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker suffices even when another creature could also block")
    void oneBlockerSatisfiesRequirement() {
        Permanent target = addCreatureReady(player1, new PensiveMinotaur());
        Permanent firstBlocker = addCreatureReady(player2, new PensiveMinotaur());
        Permanent secondBlocker = addCreatureReady(player2, new PensiveMinotaur());
        castGoldenhideOx(player1, target.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A tapped creature is not forced to block")
    void noLegalBlockerAllowsUnblockedAttack() {
        Permanent target = addCreatureReady(player1, new PensiveMinotaur());
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        blocker.tap();
        castGoldenhideOx(player1, target.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("An ordinary creature entering does not trigger constellation")
    void nonenchantmentEntryDoesNotTrigger() {
        Permanent ox = harness.addToBattlefieldAndReturn(player1, new GoldenhideOx());
        harness.setHand(player1, List.of(new PensiveMinotaur()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ox.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Removing the source does not stop an already triggered constellation ability")
    void triggerResolvesAfterSourceLeaves() {
        Permanent ox = harness.addToBattlefieldAndReturn(player1, new GoldenhideOx());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new DictateOfKarametra()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(ox);
        resolveAllTriggers();

        assertThat(target.isMustBeBlockedThisTurn()).isTrue();
    }

    private void castGoldenhideOx(com.github.laxika.magicalvibes.model.Player player,
                                  java.util.UUID targetId) {
        harness.setHand(player, List.of(new GoldenhideOx()));
        harness.addMana(player, ManaColor.GREEN, 6);
        harness.castCreature(player, 0, 0, targetId);
    }
}
