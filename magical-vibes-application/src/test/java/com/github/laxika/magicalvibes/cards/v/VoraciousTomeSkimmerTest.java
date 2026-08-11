package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoraciousTomeSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell during an opponent's turn can be paid for with 1 life to draw")
    void paysLifeToDrawOnOpponentTurnSpellCast() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new VoraciousTomeSkimmer());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining the life payment does not draw")
    void decliningLifePaymentDoesNotDraw() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new VoraciousTomeSkimmer());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Casting a spell during your own turn does not trigger")
    void ownTurnSpellCastDoesNotTrigger() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new VoraciousTomeSkimmer());
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }
}
