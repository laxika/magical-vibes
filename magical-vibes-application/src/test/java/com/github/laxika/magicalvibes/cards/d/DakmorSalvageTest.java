package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DakmorSalvage.class, Forest.class})
class DakmorSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Dakmor Salvage enters tapped")
    void entersTapped() {
        DakmorSalvage salvage = new DakmorSalvage();
        harness.setHand(player1, List.of(salvage));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dakmor Salvage produces black mana")
    void producesBlackMana() {
        Permanent salvage = new Permanent(new DakmorSalvage());
        salvage.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(salvage);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(salvage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dakmor Salvage may dredge two cards instead of drawing")
    void dredgesInsteadOfDrawing() {
        DakmorSalvage salvage = new DakmorSalvage();
        List<Card> milled = List.of(new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(salvage));
        harness.setLibrary(player1, milled);
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(salvage);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }
}
