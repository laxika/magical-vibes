package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseyRaphHotheads.class, GrizzlyBears.class})
class CaseyRaphHotheadsTest extends BaseCardTest {

    private static final String EXILE_MODE =
            "Target player exiles the top card of their library and may play it without paying its mana cost until their next end step";
    private static final String TREASURE_MODE = "Target player creates two Treasure tokens";

    @Test
    void treasureModeGivesTwoTreasuresToTheTargetPlayer() {
        castCasey();
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void bothModesTargetDifferentPlayersAndTargetPlayerMayCastExiledCardForFree() {
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));

        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(exiledCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(exiledCard.getId());

        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void bothModesCannotTargetTheSamePlayer() {
        castCasey();
        harness.handleListChoice(player1, EXILE_MODE);
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCasey() {
        harness.setHand(player1, List.of(new CaseyRaphHotheads()));
        addCaseyMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addCaseyMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
