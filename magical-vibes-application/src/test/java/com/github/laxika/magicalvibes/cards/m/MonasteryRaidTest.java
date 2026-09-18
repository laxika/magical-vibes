package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonasteryRaid.class, Forest.class, GrizzlyBears.class})
class MonasteryRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards when cast normally")
    void normalCastExilesTopTwoCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Exiles X cards when cast for freerunning")
    void freerunningCastExilesXCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, null, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Normal casting still exiles two cards when freerunning is available")
    void normalCastDoesNotUseFreerunningBranch() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new MonasteryRaid()));
        markAssassinCombatDamage();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second);
    }

    private void markAssassinCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
    }
}
