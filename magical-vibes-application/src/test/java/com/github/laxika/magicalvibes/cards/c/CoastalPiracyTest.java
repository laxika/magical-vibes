package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoastalPiracyTest extends BaseCardTest {

    private void addCoastalPiracy() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new CoastalPiracy()));
    }

    private Permanent addReadyAttacker() {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage → trigger onto stack
        harness.passBothPriorities(); // resolve the ally-combat-damage trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("A creature dealing combat damage to an opponent presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        addCoastalPiracy();
        addReadyAttacker();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingDrawsCard() {
        addCoastalPiracy();
        addReadyAttacker();

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws no card")
    void decliningDrawsNoCard() {
        addCoastalPiracy();
        addReadyAttacker();

        resolveCombat();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when the attacker is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addReadyAttacker(); // index 0 on player1's battlefield
        addCoastalPiracy();

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
