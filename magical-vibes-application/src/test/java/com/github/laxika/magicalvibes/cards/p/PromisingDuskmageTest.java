package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PromisingDuskmage.class, Assassinate.class, Forest.class})
class PromisingDuskmageTest extends BaseCardTest {

    @Test
    void drawsOneCardWhenItDiesWithOneOrMorePlusOneCounters() {
        Forest firstLibraryCard = new Forest();
        Forest secondLibraryCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard));
        Permanent duskmage = harness.addToBattlefieldAndReturn(player1, new PromisingDuskmage());
        duskmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyDuskmage(duskmage);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenItDiesWithoutAPlusOneCounter() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent duskmage = harness.addToBattlefieldAndReturn(player1, new PromisingDuskmage());

        destroyDuskmage(duskmage);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void destroyDuskmage(Permanent duskmage) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        duskmage.tap();
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castSorcery(player2, 0, duskmage.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Promising Duskmage");
    }
}
