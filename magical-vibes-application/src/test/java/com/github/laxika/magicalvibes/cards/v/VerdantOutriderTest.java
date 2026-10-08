package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrandBallGuest;
import com.github.laxika.magicalvibes.cards.h.HollowScavenger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantOutrider.class, GrandBallGuest.class, HollowScavenger.class})
class VerdantOutriderTest extends BaseCardTest {

    @Test
    void activatedAbilityPreventsPowerTwoOrLessCreaturesFromBlockingThisTurn() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent guest = addCreatureReady(player2, new GrandBallGuest());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        prepareBlockerDeclaration(outrider);

        assertThatThrownBy(() -> declareBlock(guest, outrider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    void activatedAbilityAllowsPowerThreeOrGreaterCreaturesToBlockThisTurn() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent scavenger = addCreatureReady(player2, new HollowScavenger());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        prepareBlockerDeclaration(outrider);
        declareBlock(scavenger, outrider);

        assertThat(scavenger.isBlocking()).isTrue();
    }

    @Test
    void restrictionExpiresAfterTheTurn() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent guest = addCreatureReady(player2, new GrandBallGuest());
        activateOutrider();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        prepareBlockerDeclaration(outrider);
        declareBlock(guest, outrider);
        assertThat(guest.isBlocking()).isTrue();
    }

    @Test
    void blockerPowerIsCheckedAtDeclarationRatherThanResolution() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent guest = addCreatureReady(player2, new GrandBallGuest());
        activateOutrider();

        guest.setPowerModifier(1);
        prepareBlockerDeclaration(outrider);
        declareBlock(guest, outrider);
        assertThat(guest.isBlocking()).isTrue();
    }

    @Test
    void blockerThatDropsBelowThreePowerCannotBlock() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent scavenger = addCreatureReady(player2, new HollowScavenger());
        activateOutrider();

        scavenger.setPowerModifier(-1);
        prepareBlockerDeclaration(outrider);
        assertThatThrownBy(() -> declareBlock(scavenger, outrider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    void activationDoesNotRestrictBlockersOfAnotherOutrider() {
        addCreatureReady(player1, new VerdantOutrider());
        Permanent other = addCreatureReady(player1, new VerdantOutrider());
        Permanent guest = addCreatureReady(player2, new GrandBallGuest());
        activateOutrider();

        prepareBlockerDeclaration(other);
        declareBlock(guest, other);
        assertThat(guest.isBlocking()).isTrue();
    }

    private void activateOutrider() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void prepareBlockerDeclaration(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
