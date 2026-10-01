package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormscaleAnarch.class, MistralCharger.class, TransguildCourier.class})
class StormscaleAnarchTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWhenTheDiscardedCardIsNotMulticolored() {
        harness.addToBattlefield(player1, new StormscaleAnarch());
        harness.setHand(player1, List.of(new MistralCharger()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Mistral Charger");
    }

    @Test
    void dealsFourDamageWhenTheDiscardedCardIsMulticolored() {
        harness.addToBattlefield(player1, new StormscaleAnarch());
        harness.setHand(player1, List.of(new TransguildCourier()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Transguild Courier");
    }

    @Test
    void dealsDamageToACreatureAsAnAnyTarget() {
        harness.addToBattlefield(player1, new StormscaleAnarch());
        var target = harness.addToBattlefieldAndReturn(player2, new TransguildCourier());
        harness.setHand(player1, List.of(new MistralCharger()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mistral Charger");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new StormscaleAnarch());
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
