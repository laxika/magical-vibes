package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurtiveAnalyst.class, Forest.class, GrizzlyBears.class})
class FurtiveAnalystTest extends BaseCardTest {

    @Test
    @DisplayName("The ability draws a card, then discards a card")
    void drawsThenDiscards() {
        Permanent analyst = addCreatureReady(player1, new FurtiveAnalyst());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().extracting(Object::getClass)
                .isEqualTo(Forest.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(analyst.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
