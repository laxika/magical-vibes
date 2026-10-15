package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuriWarriorOfWakanda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomeOfLegends.class, GrizzlyBears.class, Forest.class, ZuriWarriorOfWakanda.class})
class TomeOfLegendsTest extends BaseCardTest {

    @Test
    void entersWithPageCounterAndGainsCountersForCommanderEvents() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);

        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = harness.enterBattlefieldAndReturn(player1, commander);
        harness.passBothPriorities();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(2);

        commanderPermanent.setSummoningSick(false);
        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(3);
    }

    @Test
    void ignoresNoncommanderEnteringAndAttacking() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(2));
        assertThat(creature.isAttackedThisTurn()).isTrue();
        harness.passBothPriorities();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    void removesPageCounterAndDrawsACard() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(tome.getCounterCount(CounterType.PAGE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void ownCommanderEnteringUnderOpponentsControlAddsCounter() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player2, commander);
        resolveAllTriggers();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(2);
    }

    @Test
    void opponentsCommanderEnteringUnderOwnControlDoesNotAddCounter() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player2.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);
        resolveAllTriggers();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    void ownCommanderAttackingUnderOpponentsControlAddsCounter() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player2, commander);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(2);
    }

    @Test
    void opponentsCommanderAttackingUnderOwnControlDoesNotAddCounter() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        Card commander = new ZuriWarriorOfWakanda();
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player1, commander);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutPageCounter() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        tome.setCounterCount(CounterType.PAGE, 0);
        tome.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tome.isTapped()).isFalse();
        assertThat(tome.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());
        tome.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent tome = harness.enterBattlefieldAndReturn(player1, new TomeOfLegends());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tome.isTapped()).isFalse();
        assertThat(tome.getCounterCount(CounterType.PAGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
