package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VikyaScorchingStalwart.class, Forest.class, GoliathBeetle.class, SerraAngel.class})
class VikyaScorchingStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its power to a player and untaps as a cost")
    void dealsPowerDamageToPlayer() {
        Permanent vikya = addTappedVikya();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        addMana();

        activate(vikya, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(vikya.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Draws a card when excess damage is dealt to a creature")
    void drawsForExcessCreatureDamage() {
        Permanent vikya = addTappedVikya();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        addMana();

        activate(vikya, target.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Does not draw when creature damage is not excess")
    void doesNotDrawWithoutExcessDamage() {
        Permanent vikya = addTappedVikya();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        addMana();

        activate(vikya, target.getId());

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addTappedVikya() {
        Permanent vikya = harness.addToBattlefieldAndReturn(player1, new VikyaScorchingStalwart());
        vikya.setSummoningSick(false);
        vikya.tap();
        return vikya;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activate(Permanent vikya, java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vikya), null, targetId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
