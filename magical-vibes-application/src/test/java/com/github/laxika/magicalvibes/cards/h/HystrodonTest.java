package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hystrodon.class})
class HystrodonTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player may draw a card")
    void combatDamageToPlayerMayDraw() {
        Permanent hystrodon = addCreatureReady(player1, new Hystrodon());
        hystrodon.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Hystrodon()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the combat-damage trigger does not draw a card")
    void decliningCombatDamageTriggerDoesNotDraw() {
        Permanent hystrodon = addCreatureReady(player1, new Hystrodon());
        hystrodon.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Hystrodon()));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger the card draw")
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent hystrodon = addCreatureReady(player1, new Hystrodon());
        hystrodon.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Hystrodon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new Hystrodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hystrodon = findPermanent(player1, "Hystrodon");
        assertThat(hystrodon.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hystrodon));
        harness.passBothPriorities();

        assertThat(hystrodon.isFaceDown()).isFalse();
    }

}
