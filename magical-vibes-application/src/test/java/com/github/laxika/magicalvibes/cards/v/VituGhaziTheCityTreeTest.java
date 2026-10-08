package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VituGhaziTheCityTree.class)
class VituGhaziTheCityTreeTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new VituGhaziTheCityTree());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Vitu-Ghazi, the City-Tree");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void createsGreenSaprolingToken() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
    }

    @Test
    void cannotCreateTokenWithoutPayingManaCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VituGhaziTheCityTree());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityResolvesImmediatelyAndCannotBeActivatedAgainWhileTapped() {
        harness.addToBattlefield(player1, new VituGhaziTheCityTree());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tokenAbilityUsesStackAndCreatesExactlyOneUntappedTokenForItsController() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void cannotCreateTokenFromTappedLandEvenWithEnoughMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VituGhaziTheCityTree());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void colorlessManaCannotPayTheColoredPortionOfTokenCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VituGhaziTheCityTree());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
