package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
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

@CardUsed({Cytoshape.class, GrizzlyBears.class, HillGiant.class, ThrunTheLastTroll.class})
class CytoshapeTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a nonlegendary creature makes the target a temporary copy")
    void choosesNonlegendaryCreatureAndCopiesTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new ThrunTheLastTroll());
        Card cytoshape = new Cytoshape();
        harness.setHand(player1, List.of(cytoshape));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(target.getId(), chosen.getId());

        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(target.isCopyUntilEndOfTurn()).isTrue();
        assertThat(target.getCard()).isNotSameAs(target.getOriginalCard());
    }

    @Test
    @DisplayName("A legendary creature is not offered as the copy source")
    void excludesLegendaryCopySource() {
        Permanent target = addCreatureReady(player1, new ThrunTheLastTroll());
        harness.setHand(player1, List.of(new Cytoshape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNull();
        assertThat(target.isCopyUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("The copy reverts at the end of the turn")
    void copyRevertsAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new Cytoshape()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(target.isCopyUntilEndOfTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCopyUntilEndOfTurn()).isFalse();
        assertThat(target.getCard()).isSameAs(target.getOriginalCard());
    }
}
