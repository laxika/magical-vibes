package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Abyssal Gatekeeper")
@CardUsed({AbyssalGatekeeper.class, BenalishInfantry.class, RedwoodTreefolk.class})
class AbyssalGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, each player sacrifices their only creature automatically")
    void deathTriggerMakesEachPlayerSacrifice() {
        addCreatureReady(player1, new AbyssalGatekeeper());
        harness.addToBattlefield(player1, new BenalishInfantry());
        setupCombatWhereGatekeeperDies(player1, player2);

        resolveCombat(); // combat damage — Gatekeeper dies, trigger on stack
        harness.passBothPriorities(); // trigger resolves — each player sacrifices

        harness.assertInGraveyard(player1, "Abyssal Gatekeeper");
        // Controller's only remaining creature is sacrificed.
        harness.assertNotOnBattlefield(player1, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Benalish Infantry");
        // Opponent's only creature — the blocking Redwood Treefolk — is sacrificed too.
        harness.assertNotOnBattlefield(player2, "Redwood Treefolk");
        harness.assertInGraveyard(player2, "Redwood Treefolk");
    }

    @Test
    @DisplayName("A player with multiple creatures chooses which to sacrifice")
    void playerWithMultipleCreaturesChooses() {
        addCreatureReady(player1, new AbyssalGatekeeper());
        setupCombatWhereGatekeeperDies(player1, player2);
        harness.addToBattlefield(player2, new BenalishInfantry());

        resolveCombat();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());

        chooseCreatureForSacrifice(player2, "Benalish Infantry");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.assertInGraveyard(player2, "Benalish Infantry");
        harness.assertOnBattlefield(player2, "Redwood Treefolk");
    }

    @Test
    @DisplayName("The active player chooses before the other player")
    void activePlayerChoosesFirst() {
        addCreatureReady(player1, new AbyssalGatekeeper());
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player2, new BenalishInfantry());
        setupCombatWhereGatekeeperDies(player1, player2);

        resolveCombat(player1);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());

        chooseCreatureForSacrifice(player1, "Benalish Infantry");
        chooseCreatureForSacrifice(player2, "Benalish Infantry");
    }

    @Test
    @DisplayName("All players choose before their creatures are sacrificed")
    void allPlayersChooseBeforeSacrificing() {
        addCreatureReady(player2, new AbyssalGatekeeper());
        harness.addToBattlefield(player2, new BenalishInfantry());
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player1, new BenalishInfantry());
        setupCombatWhereGatekeeperDies(player2, player1);

        resolveCombat(player2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());

        chooseCreatureForSacrifice(player2, "Benalish Infantry");

        harness.assertOnBattlefield(player2, "Benalish Infantry");
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player1.getId());

        chooseCreatureForSacrifice(player1, "Benalish Infantry");

        harness.assertInGraveyard(player2, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Benalish Infantry");
        harness.assertOnBattlefield(player2, "Redwood Treefolk");
        harness.assertOnBattlefield(player1, "Redwood Treefolk");
    }

    /**
     * Attacks with the Gatekeeper into a 3/6 Redwood Treefolk blocker so it dies in combat damage.
     */
    private void setupCombatWhereGatekeeperDies(Player attacker, Player defender) {
        Permanent gatekeeper = findPermanent(attacker, "Abyssal Gatekeeper");
        gatekeeper.setSummoningSick(false);
        gatekeeper.setAttacking(true);

        Permanent blocker = addCreatureReady(defender, new RedwoodTreefolk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void chooseCreatureForSacrifice(Player player, String cardName) {
        UUID permanentId = harness.getPermanentId(player, cardName);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiPermanentChoice) {
            harness.handleMultiplePermanentsChosen(player, List.of(permanentId));
        } else {
            harness.handlePermanentChosen(player, permanentId);
        }
    }
}
