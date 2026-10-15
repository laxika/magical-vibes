package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimsonFleetCommodore.class})
class CrimsonFleetCommodoreTest extends BaseCardTest {

    @Test
    @DisplayName("Makes its controller the monarch when it enters")
    void makesControllerMonarchWhenItEnters() {
        castCrimsonFleetCommodore(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Replaces the existing monarch when it enters")
    void replacesExistingMonarch() {
        gd.monarchPlayerId = player2.getId();

        castCrimsonFleetCommodore(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Makes the opponent the monarch when the opponent controls it")
    void makesOpponentMonarch() {
        gd.monarchPlayerId = player1.getId();

        castCrimsonFleetCommodore(player2);

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Becomes monarch after entering without being cast")
    void becomesMonarchWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player1, new CrimsonFleetCommodore());

        assertThat(gd.monarchPlayerId).isNull();
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The entry trigger resolves even if the creature leaves the battlefield")
    void becomesMonarchAfterSourceLeaves() {
        gd.monarchPlayerId = player2.getId();
        Permanent commodore = harness.enterBattlefieldAndReturn(player1, new CrimsonFleetCommodore());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, commodore));

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Crimson Fleet Commodore");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Entering while already the monarch keeps the designation")
    void remainsMonarchWhenAlreadyMonarch() {
        gd.monarchPlayerId = player1.getId();

        castCrimsonFleetCommodore(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The monarch draws a card at the beginning of their end step")
    void monarchDrawsAtOwnEndStep() {
        castCrimsonFleetCommodore(player1);
        CrimsonFleetCommodore topCard = new CrimsonFleetCommodore();
        harness.setLibrary(player1, List.of(topCard, new CrimsonFleetCommodore()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Combat damage transfers the monarchy only when its trigger resolves")
    void combatDamageQueuesMonarchTransfer() {
        castCrimsonFleetCommodore(player2);
        Permanent attacker = addCreatureReady(player1, new CrimsonFleetCommodore());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        harness.resolveCombatDamage();

        harness.assertLife(player2, 15);
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        resolveAllTriggers();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    private void castCrimsonFleetCommodore(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new CrimsonFleetCommodore()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }
}
