package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MerchantOfManyHats.class)
class MerchantOfManyHatsTest extends BaseCardTest {

    @Test
    void canActivateGraveyardAbilityWithEnoughMana() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    void resolvingGraveyardAbilityReturnsThisCardToHand() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Merchant of Many Hats");
        harness.assertNotInGraveyard(player1, "Merchant of Many Hats");
    }

    @Test
    void graveyardAbilityPaysManaCost() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    void cannotActivateGraveyardAbilityWithoutEnoughMana() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
