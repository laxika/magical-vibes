package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hystrodon.class})
class HystrodonTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player may draw a card")
    void combatDamageToPlayerMayDraw() {
        Permanent hystrodon = addReadyCreature(player1, new Hystrodon());
        hystrodon.setAttacking(true);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Hystrodon())));

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
        Permanent hystrodon = addReadyCreature(player1, new Hystrodon());
        hystrodon.setAttacking(true);
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Hystrodon())));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
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

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
