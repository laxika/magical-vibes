package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesmericFiend.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Peek.class})
class MesmericFiendTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MesmericFiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB reveals the opponent's hand and prompts for a nonland card")
    void etbRevealsHandAndPromptsForChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest(), new GrizzlyBears())));

        castAndResolveEtb();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 2);
    }

    @Test
    @DisplayName("Choosing a nonland card exiles it")
    void choosingNonlandCardExilesIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));

        castAndResolveEtb();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("The exiled card returns to its owner's hand when Mesmeric Fiend leaves")
    void exiledCardReturnsWhenSourceLeaves() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveEtb();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID fiendId = harness.getPermanentId(player1, "Mesmeric Fiend");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fiendId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mesmeric Fiend");
        harness.assertInHand(player2, "Peek");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Peek"));
    }
}
