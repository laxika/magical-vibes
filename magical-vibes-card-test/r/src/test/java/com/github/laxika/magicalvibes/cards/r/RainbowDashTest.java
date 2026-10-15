package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainbowDash.class, AirElemental.class, GrizzlyBears.class, RagingGoblin.class})
class RainbowDashTest extends BaseCardTest {

    @Test
    @DisplayName("Gets 20% cooler for each attacking creature with flying or haste")
    void getsCoolerForFlyingOrHastyAttackers() {
        addCreatureReady(player1, new RainbowDash());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.playerCoolness.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Adds one of each mana color, draws, and resets at 100% coolness")
    void sonicRainboomPaysOutAtThreshold() {
        Permanent rainbowDash = addCreatureReady(player1, new RainbowDash());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        gd.playerCoolness.put(player1.getId(), 100);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rainbowDash.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerCoolness.get(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Taps but does not pay out below 100% coolness")
    void sonicRainboomDoesNothingBelowThreshold() {
        Permanent rainbowDash = addCreatureReady(player1, new RainbowDash());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.playerCoolness.put(player1.getId(), 80);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rainbowDash.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerCoolness.get(player1.getId())).isEqualTo(80);
    }

    @Test
    @DisplayName("An attacker with both flying and haste grants only 20% coolness")
    void selfAttackCountsOnceForBothKeywords() {
        addCreatureReady(player1, new RainbowDash());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerCoolness.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerCoolness.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Opposing flying attackers do not increase your coolness")
    void opposingAttackDoesNotGrantCoolness() {
        addCreatureReady(player1, new RainbowDash());
        addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerCoolness.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerCoolness.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Sonic Rainboom uses the stack and resets all coolness above 100%")
    void sonicRainboomUsesStackAndResetsExcessCoolness() {
        addCreatureReady(player1, new RainbowDash());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        gd.playerCoolness.put(player1.getId(), 140);
        gd.playerCoolness.put(player2.getId(), 60);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerCoolness.get(player1.getId())).isEqualTo(140);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerCoolness.get(player1.getId())).isZero();
        assertThat(gd.playerCoolness.get(player2.getId())).isEqualTo(60);
    }
}
