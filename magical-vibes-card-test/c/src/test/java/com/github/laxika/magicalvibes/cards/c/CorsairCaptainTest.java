package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.SpectralSailor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorsairCaptain.class, SpectralSailor.class, SavannahLions.class})
class CorsairCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("When Corsair Captain enters, it creates one Treasure token")
    void etbCreatesOneTreasureToken() {
        harness.setHand(player1, List.of(new CorsairCaptain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Other Pirates you control get +1/+1")
    void boostsOtherPiratesYouControl() {
        Permanent captain = addCreatureReady(player1, new CorsairCaptain());
        Permanent pirate = addCreatureReady(player1, new SpectralSailor());
        Permanent nonPirate = addCreatureReady(player1, new SavannahLions());
        Permanent opponentPirate = addCreatureReady(player2, new SpectralSailor());

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(pirate.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(pirate.getCard().getToughness() + 1);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(captain.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(captain.getCard().getToughness());
        assertThat(gqs.getEffectivePower(gd, nonPirate)).isEqualTo(nonPirate.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, nonPirate)).isEqualTo(nonPirate.getCard().getToughness());
        assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(opponentPirate.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, opponentPirate)).isEqualTo(opponentPirate.getCard().getToughness());
    }
    @Test
    @DisplayName("Multiple Captains boost each other and their bonuses accumulate on other Pirates")
    void multipleCaptainsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new CorsairCaptain());
        Permanent second = addCreatureReady(player1, new CorsairCaptain());
        Permanent pirate = addCreatureReady(player1, new SpectralSailor());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(3);
    }

    @Test
    @DisplayName("The created Treasure can immediately be sacrificed for one mana of any color")
    void treasureCanImmediatelyProduceMana() {
        harness.setHand(player1, List.of(new CorsairCaptain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
