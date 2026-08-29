package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClearTheLandTest extends BaseCardTest {

    @Test
    @DisplayName("Each player puts revealed lands onto the battlefield tapped and exiles the rest")
    void eachPlayerPutsLandsOntoBattlefieldAndExilesRest() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card island = new Island();
        Card shock = new Shock();
        Card mountain = new Mountain();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, bears, island, shock, mountain));

        Card swamp = new Swamp();
        Card plains = new Plains();
        Card opponentBears = new GrizzlyBears();
        Card opponentShock = new Shock();
        Card opponentForest = new Forest();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(swamp, plains, opponentBears, opponentShock, opponentForest));

        harness.setHand(player1, List.of(new ClearTheLand()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest", "Island", "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Swamp", "Plains", "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(permanent -> permanent.isTapped());

        assertThat(gd.exiledCards)
                .extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(bears, shock, opponentBears, opponentShock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library with fewer than five cards reveals all available cards")
    void libraryShorterThanFive() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, bears));
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new ClearTheLand()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
