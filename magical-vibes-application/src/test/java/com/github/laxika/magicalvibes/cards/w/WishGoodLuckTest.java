package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new WishGoodLuck()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
