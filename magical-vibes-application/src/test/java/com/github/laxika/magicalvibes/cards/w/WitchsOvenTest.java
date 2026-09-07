package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchsOven.class, GrizzlyBears.class, AirElemental.class})
class WitchsOvenTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature with toughness less than 4 creates one Food token")
    void createsOneFoodForSmallCreature() {
        harness.addToBattlefield(player1, new WitchsOven());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing a creature with toughness 4 or greater creates two Food tokens")
    void createsTwoFoodForLargeCreature() {
        harness.addToBattlefield(player1, new WitchsOven());
        addCreatureReady(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Air Elemental");
    }
}
