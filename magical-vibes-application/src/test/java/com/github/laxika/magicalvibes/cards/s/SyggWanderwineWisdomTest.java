package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyggWanderwineWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants a combat-damage draw trigger to the target creature")
    void etbGrantsCombatDamageDraw() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SyggWanderwineWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setDeck(player1, List.of(new Forest()));

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        bears.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Transforming into Wanderbrine Shield grants protection from every color")
    void backFaceGrantsProtectionUntilNextTurn() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(sygg.isTransformed()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isFalse();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
