package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AstonishingSpiderMan.class, GrizzlyBears.class, Island.class, MindRot.class})
class AstonishingSpiderManTest extends BaseCardTest {

    @Test
    void acceptingAttackTriggerDiscardsHandAndDrawsForCardsDiscardedThisTurn() {
        addReadySpiderMan();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof Island);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
    }

    @Test
    void drawCountIncludesCardsDiscardedEarlierThisTurn() {
        addReadySpiderMan();
        harness.setHand(player1, new ArrayList<>(List.of(
                new MindRot(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(4)
                .allMatch(card -> card instanceof Island);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(4);
    }

    @Test
    void decliningAttackTriggerLeavesHandUntouched() {
        addReadySpiderMan();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addReadySpiderMan() {
        addCreatureReady(player1, new AstonishingSpiderMan());
    }

    @Test
    void acceptingWithEmptyHandDrawsForEarlierDiscards() {
        addReadySpiderMan();
        harness.setHand(player1, List.of(new MindRot(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof Island);
    }

    @Test
    void opponentsDiscardsDoNotIncreaseDrawCount() {
        addReadySpiderMan();
        harness.setHand(player1, List.of(new MindRot(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof Island);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(2);
    }
}
