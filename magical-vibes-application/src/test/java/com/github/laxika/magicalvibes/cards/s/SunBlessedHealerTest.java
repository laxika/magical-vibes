package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunBlessedHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not return a card")
    void withoutKickerDoesNotReturnCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast(false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("When kicked, the ETB ability returns a target nonland permanent with mana value 2 or less")
    void kickedReturnsTargetPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The kicked ETB ability only allows nonland permanents with mana value 2 or less")
    void kickedFiltersIllegalGraveyardCards() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();
        Forest forest = new Forest();
        HolyDay holyDay = new HolyDay();
        harness.setGraveyard(player1, List.of(bears, hillGiant, forest, holyDay));
        cast(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    private void cast(boolean kicked) {
        harness.setHand(player1, List.of(new SunBlessedHealer()));
        harness.addMana(player1, ManaColor.WHITE, kicked ? 4 : 2);
        if (kicked) {
            harness.castKickedCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0);
        }
        harness.passBothPriorities();
    }
}
