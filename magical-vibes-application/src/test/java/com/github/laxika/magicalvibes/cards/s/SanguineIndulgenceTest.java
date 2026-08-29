package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SanguineIndulgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {3} less when its controller has gained at least 3 life this turn")
    void costReductionAfterGainingThreeLife() {
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new SanguineIndulgence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not get the cost reduction after gaining only 2 life this turn")
    void noCostReductionBelowThreshold() {
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new SanguineIndulgence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Returns up to two chosen creature cards and excludes noncreature cards")
    void returnsUpToTwoCreatureCards() {
        GrizzlyBears firstCreature = new GrizzlyBears();
        LlanowarElves secondCreature = new LlanowarElves();
        GrizzlyBears thirdCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, thirdCreature, new Shock()));
        harness.setHand(player1, List.of(new SanguineIndulgence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                firstCreature.getId(), secondCreature.getId(), thirdCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Sanguine Indulgence");
    }
}
