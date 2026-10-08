package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarnivalCarnage.class, SenateCourier.class, DovinGrandArbiter.class, Forest.class})
class CarnivalCarnageTest extends BaseCardTest {

    private static final int CARNIVAL = 0;
    private static final int CARNAGE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Carnival damages a creature and its controller")
    void carnivalDamagesCreatureAndController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, CARNIVAL, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Carnival can target a planeswalker")
    void carnivalDamagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.getCounters().put(CounterType.LOYALTY, 3);

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstant(player1, 0, CARNIVAL,
                List.of(planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Carnage damages the opponent and makes them discard two cards")
    void carnageDamagesAndDiscards() {
        harness.setHand(player2, List.of(new SenateCourier(), new SenateCourier(), new Forest()));
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();

        harness.castModalSorcery(player1, 0, CARNAGE, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Both halves cannot be fused")
    void rejectsFusingBothHalves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addFuseMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE,
                List.of(creature.getId(), creature.getId(), player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Carnival and Carnage reject the wrong target types")
    void modesRejectIllegalTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, CARNIVAL, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();
        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, CARNAGE, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void carnivalCannotChooseSeparateCreatureAndControllerTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, CARNIVAL,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void carnageCannotBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, CARNAGE,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void carnivalCanDamageItsOwnController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castModalInstant(player1, 0, CARNIVAL, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void carnivalDoesNotDamageControllerWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castModalInstant(player1, 0, CARNIVAL, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void carnageStillDamagesOpponentWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();
        harness.castModalSorcery(player1, 0, CARNAGE, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void carnageRejectsItsControllerAsTarget() {
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, CARNAGE,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCarnageMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addFuseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}
