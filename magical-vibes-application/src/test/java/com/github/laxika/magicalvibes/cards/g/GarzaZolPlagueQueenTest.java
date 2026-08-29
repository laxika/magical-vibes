package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GarzaZolPlagueQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a creature it damaged dies")
    void gainsCounterWhenDamagedCreatureDies() {
        Permanent garza = addReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        Permanent blocker = addReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain a counter when the damaged creature survives")
    void noCounterWhenDamagedCreatureSurvives() {
        Permanent garza = addReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        AirElemental card = new AirElemental();
        card.setToughness(6);
        Permanent blocker = addReady(player2, card);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(garza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May draw a card when it deals combat damage to a player")
    void mayDrawOnCombatDamage() {
        Permanent garza = addReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline the card draw")
    void mayDeclineDraw() {
        Permanent garza = addReady(player1, new GarzaZolPlagueQueen());
        garza.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
