package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorsairCaptain.class, FathomFleetFirebrand.class, GrizzlyBears.class})
class CorsairCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("When Corsair Captain enters, it creates one Treasure token")
    void etbCreatesOneTreasureToken() {
        harness.setHand(player1, List.of(new CorsairCaptain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Other Pirates you control get +1/+1")
    void boostsOtherPiratesYouControl() {
        Permanent captain = addCreatureReady(player1, new CorsairCaptain());
        Permanent pirate = addCreatureReady(player1, new FathomFleetFirebrand());
        Permanent nonPirate = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentPirate = addCreatureReady(player2, new FathomFleetFirebrand());

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(pirate.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(pirate.getCard().getToughness() + 1);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(captain.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(captain.getCard().getToughness());
        assertThat(gqs.getEffectivePower(gd, nonPirate)).isEqualTo(nonPirate.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, nonPirate)).isEqualTo(nonPirate.getCard().getToughness());
        assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(opponentPirate.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, opponentPirate)).isEqualTo(opponentPirate.getCard().getToughness());
    }
}
