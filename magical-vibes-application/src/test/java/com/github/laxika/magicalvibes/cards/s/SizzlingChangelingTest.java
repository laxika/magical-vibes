package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SizzlingChangelingTest extends BaseCardTest {

    private Card resolveDeathTrigger() {
        Card topCard = new Shock();
        harness.addToBattlefield(player1, new SizzlingChangeling());
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return topCard;
    }

    @Test
    @DisplayName("When Sizzling Changeling dies, the top card can be played from exile for its normal cost")
    void deathTriggerExilesTopCardAndGrantsPlayPermission() {
        Card shock = resolveDeathTrigger();

        harness.assertInGraveyard(player1, "Sizzling Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
