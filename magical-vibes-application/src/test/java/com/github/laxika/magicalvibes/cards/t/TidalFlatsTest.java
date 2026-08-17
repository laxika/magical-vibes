package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TidalFlatsTest extends BaseCardTest {

    @Test
    @DisplayName("Unpaid nonflying attackers give first strike to their blockers")
    void unpaidAttackerGrantsFirstStrikeToBlockers() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Paying prevents first strike from being granted")
    void paymentPreventsFirstStrikeGrant() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activateTidalFlats();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Flying attackers do not receive payment choices")
    void flyingAttackersAreIgnored() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent groundAttacker = addAttacker(player2, new GrizzlyBears());
        Permanent flyingAttacker = addAttacker(player2, new WindDrake());
        Permanent groundBlocker = addBlocker(player1, groundAttacker);
        Permanent flyingBlocker = addBlocker(player1, flyingAttacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(groundBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(flyingBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void activateTidalFlats() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Permanent attacker) {
        Permanent blocker = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }
}
