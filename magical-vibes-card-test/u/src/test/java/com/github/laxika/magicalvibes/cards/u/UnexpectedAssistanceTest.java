package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.MutableExplorer;
import com.github.laxika.magicalvibes.cards.m.MischievousSneakling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnexpectedAssistance.class, MutableExplorer.class, MischievousSneakling.class})
class UnexpectedAssistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards then discards one card")
    void drawsThreeThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new MutableExplorer(), new MutableExplorer(), new MutableExplorer()));
        harness.setHand(player1, List.of(new UnexpectedAssistance(), new MutableExplorer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Unexpected Assistance");
    }

    @Test
    @DisplayName("Can use convoke to help cast the spell")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new MutableExplorer());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new MutableExplorer());
        harness.setLibrary(player1, List.of(new MutableExplorer(), new MutableExplorer(), new MutableExplorer()));
        harness.setHand(player1, List.of(new UnexpectedAssistance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can choose a newly drawn card to discard while retaining the original hand card")
    void canDiscardNewlyDrawnCard() {
        MutableExplorer original = new MutableExplorer();
        MischievousSneakling drawn = new MischievousSneakling();
        MutableExplorer second = new MutableExplorer();
        MutableExplorer third = new MutableExplorer();
        harness.setLibrary(player1, List.of(drawn, second, third));
        harness.setHand(player1, List.of(new UnexpectedAssistance(), original));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn, second, third);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Unexpected Assistance");
    }

    @Test
    @DisplayName("Summoning-sick blue creatures can convoke both blue mana requirements")
    void blueCreaturesPayColoredConvokeCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MischievousSneakling());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MischievousSneakling());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new MutableExplorer(), new MutableExplorer(), new MutableExplorer()));
        harness.setHand(player1, List.of(new UnexpectedAssistance()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Unexpected Assistance");
    }

}
