package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChaliceOfTheVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X charge counters")
    void entersWithXChargeCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent chalice = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters a spell with matching mana value cast by any player")
    void countersMatchingSpell() {
        Permanent chalice = addChalice(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card spell = new GrizzlyBears();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(isOnBattlefield(player2, spell)).isFalse();
        assertThat(isInGraveyard(player2, spell)).isTrue();
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a spell with a nonmatching mana value")
    void doesNotCounterNonmatchingSpell() {
        addChalice(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card spell = new GrizzlyBears();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(isOnBattlefield(player2, spell)).isTrue();
        assertThat(isInGraveyard(player2, spell)).isFalse();
    }

    @Test
    @DisplayName("Keeps a trigger after the charge counter count changes")
    void triggerUsesCounterCountWhenSpellWasCast() {
        Permanent chalice = addChalice(player1, 2);

        Card spell = new GrizzlyBears();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        chalice.setCounterCount(CounterType.CHARGE, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(isOnBattlefield(player2, spell)).isFalse();
        assertThat(isInGraveyard(player2, spell)).isTrue();
    }

    private Permanent addChalice(Player player, int chargeCounters) {
        Permanent chalice = harness.addToBattlefieldAndReturn(player, new ChaliceOfTheVoid());
        chalice.setCounterCount(CounterType.CHARGE, chargeCounters);
        return chalice;
    }

    private boolean isOnBattlefield(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    private boolean isInGraveyard(Player player, Card card) {
        return gd.playerGraveyards.get(player.getId()).stream()
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }
}
