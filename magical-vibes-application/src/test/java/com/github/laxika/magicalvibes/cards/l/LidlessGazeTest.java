package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LidlessGaze.class, Forest.class, GrizzlyBears.class})
class LidlessGazeTest extends BaseCardTest {

    @Test
    void controllerMayPlayTheTopCardExiledFromEachLibraryWithAnyColorMana() {
        Card ownTopCard = new Forest();
        Card opponentTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new LidlessGaze()));
        addLidlessGazeMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions)
                .containsEntry(ownTopCard.getId(), player1.getId())
                .containsEntry(opponentTopCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaType).contains(opponentTopCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player2, opponentTopCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, opponentTopCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentTopCard);
    }

    @Test
    void flashbackAlsoExilesCardsAndExilesTheSpellAfterResolution() {
        Card ownTopCard = new Forest();
        Card opponentTopCard = new GrizzlyBears();
        Card lidlessGaze = new LidlessGaze();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setGraveyard(player1, List.of(lidlessGaze));
        addLidlessGazeMana();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(ownTopCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentTopCard.getId())).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(lidlessGaze);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(lidlessGaze);
    }

    private void addLidlessGazeMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
