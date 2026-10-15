package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoongloveExtractor.class, Forest.class})
class MoongloveExtractorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when it attacks")
    void drawsCardAndLosesLifeWhenItAttacks() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new MoongloveExtractor());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each attacking Extractor creates one combined draw and life-loss trigger")
    void eachAttackingExtractorTriggersOnce() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        addCreatureReady(player1, new MoongloveExtractor());
        addCreatureReady(player1, new MoongloveExtractor());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An Extractor that stays back does not trigger when another attacks")
    void nonattackingExtractorDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new MoongloveExtractor());
        addCreatureReady(player1, new MoongloveExtractor());

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The attacking controller draws and loses life, including player two")
    void playerTwoReceivesTheirAttackTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Forest drawn = new Forest();
        harness.setLibrary(player2, List.of(drawn));
        addCreatureReady(player2, new MoongloveExtractor());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The attack trigger resolves after its source dies")
    void attackTriggerSurvivesSourceLeavingBattlefield() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        var extractor = addCreatureReady(player1, new MoongloveExtractor());

        declareAttackers(List.of(0));
        extractor.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Moonglove Extractor");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player1, 19);
    }
}
