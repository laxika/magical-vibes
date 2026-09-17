package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeaconOfDestiny.class, FugitiveWizard.class, Shock.class})
class BeaconOfDestinyTest extends BaseCardTest {

    @Test
    void redirectsChosenSourcesPlayerDamageToBeacon() {
        Permanent beacon = addCreatureReady(player1, new BeaconOfDestiny());
        Permanent attacker = addCreatureReady(player2, new FugitiveWizard());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, attacker);
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(beacon.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotRedirectChosenSourcesDamageToCreatures() {
        Permanent beacon = addCreatureReady(player1, new BeaconOfDestiny());
        Permanent attacker = addCreatureReady(player2, new FugitiveWizard());
        Permanent victim = addCreatureReady(player1, new BeaconOfDestiny());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, attacker);
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, victim), indexOf(player2, attacker))));
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
        assertThat(beacon.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void doesNotRedirectDamageFromAnotherSource() {
        Permanent beacon = addCreatureReady(player1, new BeaconOfDestiny());
        Permanent chosenSource = addCreatureReady(player2, new FugitiveWizard());
        Permanent otherSource = addCreatureReady(player2, new FugitiveWizard());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, chosenSource);
        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(beacon.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void redirectsOnlyTheNextDamageEventFromChosenSource() {
        Permanent beacon = addCreatureReady(player1, new BeaconOfDestiny());
        Permanent attacker = addCreatureReady(player2, new FugitiveWizard());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, attacker);
        attacker.setAttacking(true);
        resolveCombat(player2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(beacon.getMarkedDamage()).isEqualTo(1);

        attacker.untap();
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(beacon.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void shieldExpiresAtEndOfTurn() {
        Permanent beacon = addCreatureReady(player1, new BeaconOfDestiny());
        Permanent attacker = addCreatureReady(player2, new FugitiveWizard());
        int lifeBefore = gd.getLife(player1.getId());

        activateAndChooseSource(beacon, attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(beacon.getMarkedDamage()).isZero();
    }

    @Test
    void canChooseASpellOnTheStackAsTheDamageSource() {
        Permanent beacon = addCreatureReady(player1, new BeaconOfDestiny());
        Shock shock = new Shock();
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.activateAbility(player1, indexOf(player1, beacon), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(beacon.getMarkedDamage()).isEqualTo(2);
    }

    private void activateAndChooseSource(Permanent beacon, Permanent source) {
        harness.activateAbility(player1, indexOf(player1, beacon), 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
