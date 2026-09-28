package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackWidowIntelExpert.class, GrizzlyBears.class})
class BlackWidowIntelExpertTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes both players draw two cards")
    void combatDamageMakesBothPlayersDrawTwoCards() {
        Card player1Drawn1 = new GrizzlyBears();
        Card player1Drawn2 = new GrizzlyBears();
        Card player2Drawn1 = new GrizzlyBears();
        Card player2Drawn2 = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Drawn1, player1Drawn2));
        harness.setLibrary(player2, List.of(player2Drawn1, player2Drawn2));
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BlackWidowIntelExpert());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Drawn1, player1Drawn2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Drawn1, player2Drawn2);
    }

    @Test
    @DisplayName("No combat damage means neither player draws")
    void noCombatDamageMeansNeitherPlayerDraws() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);
        Permanent widow = addCreatureReady(player1, new BlackWidowIntelExpert());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(widow))));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
