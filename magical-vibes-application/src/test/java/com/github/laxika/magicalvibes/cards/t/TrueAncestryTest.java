package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrueAncestry.class, GrizzlyBears.class, MindStone.class, HolyDay.class})
class TrueAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent card to hand and creates a Clue")
    void returnsPermanentCardAndCreatesClue() {
        Card target = new MindStone();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TrueAncestry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "True Ancestry");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Choosing no target still creates a Clue")
    void choosingNoTargetStillCreatesClue() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TrueAncestry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "True Ancestry");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Only permanent cards can be selected")
    void onlyPermanentCardsCanBeSelected() {
        Card permanent = new GrizzlyBears();
        Card nonPermanent = new HolyDay();
        harness.setGraveyard(player1, List.of(permanent, nonPermanent));
        harness.setHand(player1, List.of(new TrueAncestry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(permanent.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
