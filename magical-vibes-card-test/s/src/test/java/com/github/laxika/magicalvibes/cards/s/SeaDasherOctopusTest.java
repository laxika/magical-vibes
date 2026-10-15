package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaDasherOctopus.class, Forest.class, GrizzlyBears.class})
class SeaDasherOctopusTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addAttacker(player1, new SeaDasherOctopus());

        resolveUnblockedCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when blocked")
    void doesNotDrawWhenBlocked() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent octopus = addAttacker(player1, new SeaDasherOctopus());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(octopus))));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can be cast during the opponent's upkeep using flash")
    void castsDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SeaDasherOctopus(), "{1}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sea-Dasher Octopus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each attacking Octopus draws one card independently")
    void twoOctopusesDrawTwoCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addAttacker(player1, new SeaDasherOctopus());
        addAttacker(player1, new SeaDasherOctopus());

        resolveUnblockedCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw trigger resolves for its controller after the source leaves")
    void drawTriggerSurvivesSourceLeaving() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        Permanent octopus = addAttacker(player2, new SeaDasherOctopus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        gd.playerBattlefields.get(player2.getId()).remove(octopus);
        gd.playerGraveyards.get(player2.getId()).add(octopus.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void resolveUnblockedCombat() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = addCreatureReady(player, card);
        attacker.setAttacking(true);
        return attacker;
    }
}
