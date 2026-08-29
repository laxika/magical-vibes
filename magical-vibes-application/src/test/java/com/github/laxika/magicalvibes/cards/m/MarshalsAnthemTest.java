package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarshalsAnthemTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it returns no creatures")
    void returnsNoCreaturesWithoutMultikicker() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Marshal's Anthem");
    }

    @Test
    @DisplayName("Returns up to one creature for one multikicker payment")
    void returnsOneCreatureForOneMultikickerPayment() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castWithMultikickerPayments(List.of("{1}{W}"));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears).isNotNull();
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Returns fewer than the multikicker cap and only offers creature cards")
    void returnsUpToTwoCreatureCards() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new MarshalsAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castWithMultikickerPayments(List.of("{1}{W}", "{1}{W}"));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    private void castWithMultikickerPayments(List<String> payments) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
    }
}
