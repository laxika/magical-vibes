package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeiNightRaidersTest extends BaseCardTest {

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
    @DisplayName("Dealing combat damage to an opponent forces them to discard a chosen card")
    void combatDamageTriggersDiscard() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent raiders = addReadyCreature(player1, new WeiNightRaiders());
        raiders.setAttacking(true);

        resolveCombat();

        // The damaged opponent chooses which card to discard.
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No discard when the opponent has an empty hand")
    void noDiscardWhenEmptyHand() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of());

        Permanent raiders = addReadyCreature(player1, new WeiNightRaiders());
        raiders.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when the Raiders are blocked and deal no damage to the player")
    void noTriggerWhenBlocked() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        Permanent raiders = addReadyCreature(player1, new WeiNightRaiders());
        raiders.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Opponent takes 2 combat damage from the unblocked Raiders")
    void opponentTakesCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent raiders = addReadyCreature(player1, new WeiNightRaiders());
        raiders.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
