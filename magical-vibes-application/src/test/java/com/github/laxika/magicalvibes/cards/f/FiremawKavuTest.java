package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiremawKavu.class, HavenwoodWurm.class})
class FiremawKavuTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to its target and leaves-the-battlefield deals 4 to a new target")
    void entersAndLeavesWithIndependentDamageTargets() {
        Permanent enterTarget = addCreatureReady(player2, new HavenwoodWurm());
        Permanent leaveTarget = addCreatureReady(player2, new HavenwoodWurm());
        Permanent firemaw = castFiremaw(enterTarget.getId());

        assertThat(enterTarget.getMarkedDamage()).isEqualTo(2);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firemaw));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, leaveTarget.getId());
        harness.passBothPriorities();

        assertThat(leaveTarget.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining echo sacrifices Firemaw Kavu")
    void decliningEchoSacrificesFiremawKavu() {
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());
        castFiremaw(target.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertNotOnBattlefield(player1, "Firemaw Kavu");
        harness.assertInGraveyard(player1, "Firemaw Kavu");
    }

    @Test
    @DisplayName("Paying echo keeps Firemaw Kavu and echo does not trigger again")
    void payingEchoKeepsFiremawKavuAndIsOneShot() {
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());
        castFiremaw(target.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Firemaw Kavu");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Firemaw Kavu");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());
        castFiremaw(target.getId());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Firemaw Kavu");
    }

    @Test
    @DisplayName("Firemaw Kavu cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FiremawKavu()));
        addFiremawCastMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    @Test
    @DisplayName("Echo still triggers when the entry damage target becomes illegal")
    void echoTriggersAfterEntryDamageFailsToResolve() {
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());
        harness.setHand(player1, List.of(new FiremawKavu()));
        addFiremawCastMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Firemaw Kavu");

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Returning Firemaw Kavu to hand triggers four damage")
    void returningToHandTriggersDamage() {
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());
        Permanent firemaw = castFiremaw(target.getId());
        Permanent leaveTarget = addCreatureReady(player2, new HavenwoodWurm());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, firemaw));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, leaveTarget.getId());
        harness.passBothPriorities();

        assertThat(leaveTarget.getMarkedDamage()).isEqualTo(4);
        harness.assertInHand(player1, "Firemaw Kavu");
        harness.assertNotOnBattlefield(player1, "Firemaw Kavu");
    }

    @Test
    @DisplayName("Exiling Firemaw Kavu triggers four damage")
    void exilingTriggersDamage() {
        Permanent target = addCreatureReady(player2, new HavenwoodWurm());
        Permanent firemaw = castFiremaw(target.getId());
        Permanent leaveTarget = addCreatureReady(player2, new HavenwoodWurm());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToExile(gd, firemaw));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, leaveTarget.getId());
        harness.passBothPriorities();

        assertThat(leaveTarget.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Firemaw Kavu");
        harness.assertNotInGraveyard(player1, "Firemaw Kavu");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getName().equals("Firemaw Kavu")
                        && entry.ownerId().equals(player1.getId()));
    }

    private Permanent castFiremaw(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FiremawKavu()));
        addFiremawCastMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Firemaw Kavu");
    }

    private void addFiremawCastMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
