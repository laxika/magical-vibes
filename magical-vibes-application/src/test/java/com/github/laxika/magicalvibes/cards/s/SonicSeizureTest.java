package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonicSeizure.class, SengirVampire.class})
class SonicSeizureTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a random card as a cost and deals 3 damage to any target")
    void discardsRandomCardAndDealsDamage() {
        harness.setHand(player1, List.of(new SonicSeizure(), new SengirVampire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Sengir Vampire");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals exactly 3 damage to a creature target")
    void dealsThreeDamageToCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setHand(player1, List.of(new SonicSeizure(), new SengirVampire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Sengir Vampire");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be cast when there is no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new SonicSeizure()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card at random");
    }
}
