package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TrudgeGarden.class)
class TrudgeGardenTest extends BaseCardTest {

    @Test
    void payingAfterGainingLifeCreatesTrampleFungusBeast() {
        harness.addToBattlefield(player1, new TrudgeGarden());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gainLife(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Fungus Beast").getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.FUNGUS, CardSubtype.BEAST);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningPaymentDoesNotCreateToken() {
        harness.addToBattlefield(player1, new TrudgeGarden());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gainLife(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Fungus Beast")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.passBothPriorities();
    }

    @Test
    void gainingSeveralLifeCreatesOnlyOneTokenForOnePayment() {
        harness.addToBattlefield(player1, new TrudgeGarden());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gainLife(5);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fungus Beast")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void separateLifeGainsCanEachCreateATokenInTheSameTurn() {
        harness.addToBattlefield(player1, new TrudgeGarden());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gainLife(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gainLife(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fungus Beast")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentGainingLifeDoesNotTriggerGarden() {
        harness.addToBattlefield(player1, new TrudgeGarden());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Fungus Beast")).isEmpty();
    }

    @Test
    void insufficientManaDoesNotCreateTokenOrSpendPartialPayment() {
        harness.addToBattlefield(player1, new TrudgeGarden());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gainLife(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fungus Beast")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void gainLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), amount));
    }
}
