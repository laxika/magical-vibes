package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Grief.class, Forest.class, Peek.class, Duress.class})
class GriefTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: ETB reveals an opponent's hand and discards a chosen nonland card")
    void hardcastDiscardsChosenNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Forest())));
        harness.setHand(player1, List.of(new Grief()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player1, "Grief");
    }

    @Test
    @DisplayName("Evoke: exiles a black card, resolves the ETB, and sacrifices Grief")
    void evokeExilesBlackCardDiscardsThenSacrifices() {
        harness.setHand(player2, List.of(new Peek()));
        harness.setHand(player1, new ArrayList<>(List.of(new Grief(), new Duress())));

        harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Duress");
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInGraveyard(player1, "Grief");
        harness.assertNotOnBattlefield(player1, "Grief");
    }

    @Test
    @DisplayName("ETB has no discard choice when the opponent has only lands")
    void noChoiceForLandOnlyHand() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Grief()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player1, "Grief");
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new Grief()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
