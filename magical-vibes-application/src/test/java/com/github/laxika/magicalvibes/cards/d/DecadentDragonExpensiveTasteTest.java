package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ExpensiveTaste;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecadentDragonExpensiveTaste.class, ExpensiveTaste.class, GrizzlyBears.class})
class DecadentDragonExpensiveTasteTest extends BaseCardTest {

    @Test
    void attackingCreatesTreasure() {
        addCreatureReady(player1, new DecadentDragonExpensiveTaste());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void adventureExilesTopCardsAndLetsItsControllerCastThemLater() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        DecadentDragonExpensiveTaste card = new DecadentDragonExpensiveTaste();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(first.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetItsController() {
        DecadentDragonExpensiveTaste card = new DecadentDragonExpensiveTaste();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
