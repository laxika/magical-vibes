package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrOfSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals green cards, gives target creature +X/+X, and sacrifices itself")
    void revealsGreenCardsAndBoostsTargetCreature() {
        Card firstGreenCard = new GiantGrowth();
        Card secondGreenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(firstGreenCard, secondGreenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, target.getId());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstGreenCard.getId(), secondGreenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId(), secondGreenCard.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstGreenCard, secondGreenCard);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot reveal more green cards than are in hand")
    void cannotRevealMoreGreenCardsThanAreInHand() {
        harness.setHand(player1, List.of(new RagingGoblin()));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
