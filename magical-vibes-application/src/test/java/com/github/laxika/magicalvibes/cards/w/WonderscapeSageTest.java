package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WonderscapeSage.class, Desert.class, Island.class})
class WonderscapeSageTest extends BaseCardTest {

    @Test
    @DisplayName("Returning a basic land draws, then requires a discard")
    void basicLandRequiresDiscard() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returning a land with a nonbasic land type skips the discard")
    void nonbasicLandSkipsDiscard() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new Desert());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Island", "Desert");
    }
}
