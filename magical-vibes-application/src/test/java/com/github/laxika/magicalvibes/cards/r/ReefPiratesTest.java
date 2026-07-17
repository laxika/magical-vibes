package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReefPiratesTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Dealing combat damage to an opponent makes that player mill a card")
    void combatDamageTriggersMill() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).add(0, new GrizzlyBears());

        Permanent pirates = addReadyCreature(player1, new ReefPirates());
        pirates.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No mill when Reef Pirates is blocked and deals no damage to a player")
    void noMillWhenBlocked() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).add(0, new GrizzlyBears());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        Permanent pirates = addReadyCreature(player1, new ReefPirates());
        pirates.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // No combat damage reached the player, so nothing was milled from the library.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Defender takes combat damage from unblocked Reef Pirates")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).add(0, new GrizzlyBears());

        Permanent pirates = addReadyCreature(player1, new ReefPirates());
        pirates.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
