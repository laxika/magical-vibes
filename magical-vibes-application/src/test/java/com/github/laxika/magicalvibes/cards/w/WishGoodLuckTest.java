package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WishGoodLuck.class, GrizzlyBears.class})
class WishGoodLuckTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food, a tapped Treasure, and a 3/2 Vehicle token")
    void createsAllTokens() {
        castWishGoodLuck();

        assertThat(findPermanents(player1, "Food")).hasSize(1);

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isTrue();

        Permanent vehicle = findPermanent(player1, "Vehicle");
        assertThat(vehicle.getCard().getSubtypes()).contains(CardSubtype.VEHICLE);
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Vehicle token can be crewed for one")
    void vehicleCanBeCrewed() {
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        castWishGoodLuck();
        Permanent vehicle = findPermanent(player1, "Vehicle");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private void castWishGoodLuck() {
        harness.castFromHand(player1, new WishGoodLuck(), "{R}{G}");
        harness.passBothPriorities();
    }

    @Test
    void foodCanBeSacrificedImmediatelyForThreeLife() {
        castWishGoodLuck();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent food = findPermanent(player1, "Food");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void tappedTreasureCannotProduceManaUntilUntapped() {
        castWishGoodLuck();
        Permanent treasure = findPermanent(player1, "Treasure");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.performUntapStep(player1);
        harness.activateAbility(player1, index, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCanCrewVehicle() {
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(crew.isSummoningSick()).isTrue();
        castWishGoodLuck();
        Permanent vehicle = findPermanent(player1, "Vehicle");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, vehicle)).isEmpty();
    }

    @Test
    void vehicleCannotBeCrewedWithoutCreatures() {
        castWishGoodLuck();
        Permanent vehicle = findPermanent(player1, "Vehicle");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
