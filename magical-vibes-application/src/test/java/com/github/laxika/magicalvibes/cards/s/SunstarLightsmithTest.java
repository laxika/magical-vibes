package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunstarLightsmith.class, LightningBolt.class})
class SunstarLightsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself and draws a card for your second spell each turn")
    void secondSpellAddsCounterAndDrawsCard() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        Card drawnCard = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Does not trigger for your third spell")
    void doesNotTriggerForThirdSpell() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsSecondSpellDoesNotTrigger() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void castingLightsmithAsFirstSpellCountsTowardItsTrigger() {
        Card drawnCard = new LightningBolt();
        harness.setHand(player1, List.of(new SunstarLightsmith(), new LightningBolt()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent lightsmith = findPermanent(player1, "Sunstar Lightsmith");
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void castingLightsmithAsSecondSpellDoesNotTriggerItself() {
        Card libraryCard = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), new SunstarLightsmith()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sunstar Lightsmith")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    void drawsEvenIfLightsmithDiesBeforeTriggerResolves() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        Card drawnCard = new LightningBolt();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, lightsmith.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sunstar Lightsmith");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void triggersAgainOnOpponentsTurnWithAFreshSpellCount() {
        Permanent lightsmith = addCreatureReady(player1, new SunstarLightsmith());
        Card firstDraw = new LightningBolt();
        Card secondDraw = new LightningBolt();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(lightsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }
}
