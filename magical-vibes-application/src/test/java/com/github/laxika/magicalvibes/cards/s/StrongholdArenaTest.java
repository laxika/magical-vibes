package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongholdArena.class, GrizzlyBears.class})
class StrongholdArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Green kicker gains 3 life when Stronghold Arena enters")
    void greenKickerGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.GREEN);

        castArena(true, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("White kicker gains 3 life when Stronghold Arena enters")
    void whiteKickerGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.WHITE);

        castArena(false, List.of("{W}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Both kicker payments gain 6 life when Stronghold Arena enters")
    void bothKickersGainLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.GREEN, ManaColor.WHITE);

        castArena(true, List.of("{W}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Combat damage trigger resolves once for multiple creatures and loses the revealed mana value")
    void combatDamageTriggerIsBatched() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new StrongholdArena());

        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
    }

    private void addMana(ManaColor... colors) {
        for (ManaColor color : colors) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castArena(boolean kicked, List<String> repeatedAdditionalCosts) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                repeatedAdditionalCosts, false);
    }
}
