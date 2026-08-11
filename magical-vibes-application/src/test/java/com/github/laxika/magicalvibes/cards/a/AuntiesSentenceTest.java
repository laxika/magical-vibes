package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuntiesSentenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode gives target creature -2/-2 until end of turn")
    void creatureModeGivesMinusTwoMinusTwo() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(4);
        bearCard.setToughness(4);
        Permanent bear = addCreatureReady(player2, bearCard);

        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Hand mode discards a chosen nonland permanent card")
    void handModeDiscardsChosenNonlandPermanent() {
        Card bear = new GrizzlyBears();
        Card peek = new Peek();
        Card forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(bear, peek, forest)));

        harness.setHand(player1, List.of(new AuntiesSentence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Peek");
        harness.assertInHand(player2, "Forest");
    }
}
