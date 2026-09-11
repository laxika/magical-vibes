package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusionaryMask.class, GrizzlyBears.class})
class IllusionaryMaskTest extends BaseCardTest {

    @Test
    void castsAQualifyingCreatureFaceDown() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isFaceDown()).isTrue();
        assertThat(bears.getFaceDownPower()).isEqualTo(2);
        assertThat(bears.getFaceDownToughness()).isEqualTo(2);

        bears.addMarkedDamage(null, 1);
        assertThat(bears.isFaceDown()).isFalse();
    }

    @Test
    void doesNotOfferAColoredCostTheSpentManaCannotPay() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
