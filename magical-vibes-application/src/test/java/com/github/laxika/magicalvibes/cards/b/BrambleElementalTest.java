package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MoldervineCloak;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrambleElemental.class, MoldervineCloak.class})
class BrambleElementalTest extends BaseCardTest {

    @Test
    void auraAttachedToBrambleElementalCreatesTwoSaprolings() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachMoldervineCloak(player1, elemental);

        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(2)
                .allSatisfy(saproling -> {
                    assertThat(saproling.getCard().isToken()).isTrue();
                    assertThat(saproling.getCard().getPower()).isEqualTo(1);
                    assertThat(saproling.getCard().getToughness()).isEqualTo(1);
                    assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                });
    }

    @Test
    void opponentAuraAttachedToBrambleElementalCreatesTokensForElementalsController() {
        Permanent elemental = addCreatureReady(player1, new BrambleElemental());

        attachMoldervineCloak(player2, elemental);

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    void auraAttachedToAnotherBrambleElementalDoesNotTriggerThisOne() {
        addCreatureReady(player1, new BrambleElemental());
        Permanent otherElemental = addCreatureReady(player2, new BrambleElemental());

        attachMoldervineCloak(player1, otherElemental);

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).hasSize(2);
    }

    private void attachMoldervineCloak(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new MoldervineCloak()));
        harness.addMana(controller, ManaColor.GREEN, 1);
        harness.addMana(controller, ManaColor.COLORLESS, 2);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
