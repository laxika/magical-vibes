package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Abyssal Gatekeeper")
class AbyssalGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, each player sacrifices their only creature automatically")
    void deathTriggerMakesEachPlayerSacrifice() {
        harness.addToBattlefield(player1, new AbyssalGatekeeper());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupCombatWhereGatekeeperDies();

        harness.passBothPriorities(); // combat damage — Gatekeeper dies, trigger on stack
        harness.passBothPriorities(); // trigger resolves — each player sacrifices

        harness.assertInGraveyard(player1, "Abyssal Gatekeeper");
        // Controller's only remaining creature is sacrificed.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Opponent's only creature — the blocking Giant Spider — is sacrificed too.
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A player with multiple creatures chooses which to sacrifice")
    void playerWithMultipleCreaturesChooses() {
        harness.addToBattlefield(player1, new AbyssalGatekeeper());
        setupCombatWhereGatekeeperDies();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    /**
     * Attacks with the Gatekeeper into a 3/3 Giant Spider blocker so it dies in combat damage.
     */
    private void setupCombatWhereGatekeeperDies() {
        GameData gd = harness.getGameData();
        Permanent gatekeeper = findPermanent(player1, "Abyssal Gatekeeper");
        gatekeeper.setSummoningSick(false);
        gatekeeper.setAttacking(true);

        Permanent blocker = new Permanent(new GiantSpider());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
