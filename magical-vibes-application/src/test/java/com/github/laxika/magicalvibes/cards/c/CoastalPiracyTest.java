package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoastalPiracy.class, GrizzlyBears.class, FreshVolunteers.class})
class CoastalPiracyTest extends BaseCardTest {

    private void addCoastalPiracy() {
        harness.addToBattlefield(player1, new CoastalPiracy());
    }

    private Permanent addReadyAttacker() {
        return addReadyAttacker(player1);
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.setAttacking(true);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        resolveCombatAndTrigger(player1);
    }

    private void resolveCombatAndTrigger(Player attackingPlayer) {
        Player defendingPlayer = attackingPlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.setLife(defendingPlayer, 20);
        resolveCombat(attackingPlayer);
        harness.passBothPriorities(); // resolve the ally-combat-damage trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("A creature dealing combat damage to an opponent presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        addCoastalPiracy();
        addReadyAttacker();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingDrawsCard() {
        addCoastalPiracy();
        addReadyAttacker();

        resolveCombatAndTrigger();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws no card")
    void decliningDrawsNoCard() {
        addCoastalPiracy();
        addReadyAttacker();

        resolveCombatAndTrigger();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A creature controlled by an opponent dealing combat damage does not trigger")
    void opponentCreatureDamageDoesNotTrigger() {
        addCoastalPiracy();
        addReadyAttacker(player2);

        resolveCombatAndTrigger(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Each creature dealing combat damage creates a separate may-draw choice")
    void eachCreatureCreatesSeparateMayChoice() {
        addCoastalPiracy();
        addReadyAttacker();
        addReadyAttacker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        acceptPendingMayDraw();
        acceptPendingMayDraw();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void acceptPendingMayDraw() {
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) == null) {
            resolveAllTriggers();
        }
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("No trigger when the attacker is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addReadyAttacker(); // index 0 on player1's battlefield
        addCoastalPiracy();

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature controlled by another player does not trigger Coastal Piracy")
    void opponentCreatureDoesNotTrigger() {
        addCoastalPiracy();
        Permanent attacker = addCreatureReady(player2, new FreshVolunteers());
        attacker.setAttacking(true);

        harness.setLife(player1, 20);
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
