package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightstallInquisitor.class, GrizzlyBears.class, Forest.class})
class LightstallInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent chooses a card from hand to exile and may play it")
    void eachOpponentChoosesCardAndGetsPlayPermission() {
        GrizzlyBears bears = new GrizzlyBears();

        exileWithInquisitor(bears);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Spells cast from Lightstall Inquisitor's exile permission cost {1} more")
    void spellCastThisWayCostsOneMore() {
        GrizzlyBears bears = new GrizzlyBears();
        exileWithInquisitor(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land played from Lightstall Inquisitor's exile permission enters tapped")
    void landPlayedThisWayEntersTapped() {
        Forest forest = new Forest();
        exileWithInquisitor(forest);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, forest.getId());

        Permanent playedForest = findPermanent(player2, "Forest");
        assertThat(playedForest.isTapped()).isTrue();
    }

    private void exileWithInquisitor(Card card) {
        harness.setHand(player1, List.of(new LightstallInquisitor()));
        harness.setHand(player2, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
    }
}
