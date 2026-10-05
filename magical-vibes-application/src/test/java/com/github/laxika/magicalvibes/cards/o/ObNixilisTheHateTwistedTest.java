package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnlikelyAid;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObNixilisTheHateTwisted.class, CounselOfTheSoratami.class, Forest.class,
        GrizzlyBears.class, UnlikelyAid.class, Unsummon.class})
class ObNixilisTheHateTwistedTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage whenever an opponent draws a card")
    void damagesOpponentForEachCardDrawn() {
        addReadyOb(player1);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Destroying a creature makes its controller draw two cards")
    void destroysCreatureAndMakesItsControllerDrawTwo() {
        Permanent obNixilis = addReadyOb(player1);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(obNixilis.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);

        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The loyalty ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyOb(player1);
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Drawing cards on its controller's behalf does not trigger the ability")
    void controllerDrawDoesNotTrigger() {
        addReadyOb(player1);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }


    @Test
    @DisplayName("Its controller can destroy their own creature and draw without taking damage")
    void destroysOwnCreatureWithoutDrawDamage() {
        addReadyOb(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An indestructible creature survives but its controller still draws and takes damage")
    void indestructibleTargetStillCausesDraws() {
        addReadyOb(player1);
        harness.setHand(player2, List.of(new UnlikelyAid()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An illegal target prevents both destruction and the two draws")
    void removedTargetDoesNotCauseDraws() {
        addReadyOb(player1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Spending the last two loyalty still destroys and draws but causes no draw damage")
    void lastTwoLoyaltyDoesNotCauseDrawDamage() {
        Permanent obNixilis = addReadyOb(player1);
        obNixilis.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ob Nixilis, the Hate-Twisted");
        harness.assertInGraveyard(player1, "Ob Nixilis, the Hate-Twisted");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addReadyOb(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ObNixilisTheHateTwisted());
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
