package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.ProfessorZeiAnthropologist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerchantOfManyHats.class, ProfessorZeiAnthropologist.class})
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

    @Test
    void returnsOnlyTheActivatedCopy() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        MerchantOfManyHats other = new MerchantOfManyHats();
        MerchantOfManyHats opponentsMerchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant, other));
        harness.setGraveyard(player2, List.of(opponentsMerchant));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(merchant).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsMerchant);
    }

    @Test
    void multipleActivationsReturnTheCardOnlyOnce() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsOnlyOnce(merchant);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTheBlackRequirementWithColorlessMana() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        harness.setGraveyard(player1, List.of(merchant));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(merchant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void olderActivationDoesNotReturnTheCardAfterItLeavesAndReentersTheGraveyard() {
        MerchantOfManyHats merchant = new MerchantOfManyHats();
        MerchantOfManyHats drawn = new MerchantOfManyHats();
        var professor = harness.addToBattlefieldAndReturn(player1, new ProfessorZeiAnthropologist());
        professor.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, List.of(merchant));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(merchant);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(merchant);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(merchant);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn).doesNotContain(merchant);
        assertThat(gd.stack).isEmpty();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
