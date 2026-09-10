package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CravenGiant;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
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

@CardUsed({TemptingLicid.class, SpinedWurm.class, VolrathsStronghold.class, CravenGiant.class})
class TemptingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Licid ability attaches it to the target creature as an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent host = addCreatureReady(player1, new SpinedWurm());
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
        assertThat(licid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature has Lure")
    void enchantedCreatureMustBeBlockedByAllAbleCreatures() {
        Permanent host = addCreatureReady(player1, new SpinedWurm());
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, host.getId());
        harness.passBothPriorities();

        host.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        addCreatureReady(player2, new SpinedWurm());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The creature form does not have Lure")
    void creatureFormDoesNotRequireBlockers() {
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        licid.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        addCreatureReady(player2, new SpinedWurm());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Paying the end cost returns the Licid to creature form")
    void endCostRevertsLicidToCreature() {
        Permanent host = addCreatureReady(player1, new SpinedWurm());
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, host.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).as("ending a Licid effect is a special action").isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("The transformation ability cannot target a noncreature permanent")
    void cannotTargetLand() {
        addCreatureReady(player1, new TemptingLicid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("If the target leaves before resolution, the Licid stays a creature")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        Permanent host = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("An attached Licid is put into its owner's graveyard when the host leaves")
    void orphanedAuraIsPutIntoGraveyard() {
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        Permanent host = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(licid);
        harness.assertInGraveyard(player1, "Tempting Licid");
    }

    @Test
    @DisplayName("A creature unable to block is not required to block the enchanted creature")
    void unableBlockerIsNotRequired() {
        Permanent host = addCreatureReady(player1, new SpinedWurm());
        Permanent licid = addCreatureReady(player1, new TemptingLicid());
        Permanent unableBlocker = addCreatureReady(player2, new CravenGiant());
        Permanent ableBlocker = addCreatureReady(player2, new SpinedWurm());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, host.getId());
        harness.passBothPriorities();

        host.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(unableBlocker.isBlocking()).isFalse();
        assertThat(ableBlocker.isBlocking()).isTrue();
    }
}
