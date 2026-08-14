package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HighSocietyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature")
    void attackingOffersSacrifice() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), secondBears.getId());
        assertThat(choice.validIds()).doesNotContain(hunter.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on High-Society Hunter")
    void sacrificingAnotherCreaturePutsCounter() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the battlefield unchanged")
    void decliningSacrificeDoesNothing() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("With no other creature, attacking does not create a sacrifice choice")
    void noOtherCreatureDoesNothing() {
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws a card when another nontoken creature dies")
    void drawsOnNontokenCreatureDeath() {
        harness.addToBattlefield(player1, new HighSocietyHunter());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when a token creature dies")
    void tokenDeathDoesNotDraw() {
        harness.addToBattlefield(player1, new HighSocietyHunter());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        Card tokenBear = new GrizzlyBears();
        tokenBear.setToken(true);
        harness.addToBattlefield(player2, tokenBear);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
