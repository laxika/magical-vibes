package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingCelestialWind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CelestialMessengerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls a Yanling planeswalker")
    void getsBoostWithYanlingPlaneswalker() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        int basePower = gqs.getEffectivePower(gd, messenger);
        int baseToughness = gqs.getEffectiveToughness(gd, messenger);

        harness.addToBattlefield(player1, new MuYanlingCelestialWind());

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not get the boost without a Yanling planeswalker")
    void noBoostWithoutYanlingPlaneswalker() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        int basePower = gqs.getEffectivePower(gd, messenger);
        int baseToughness = gqs.getEffectiveToughness(gd, messenger);

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("An opponent's Yanling planeswalker does not grant the boost")
    void opponentYanlingDoesNotCount() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        int basePower = gqs.getEffectivePower(gd, messenger);
        int baseToughness = gqs.getEffectiveToughness(gd, messenger);

        harness.addToBattlefield(player2, new MuYanlingCelestialWind());

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("A non-planeswalker with the Yanling subtype does not grant the boost")
    void nonPlaneswalkerYanlingDoesNotCount() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        int basePower = gqs.getEffectivePower(gd, messenger);
        int baseToughness = gqs.getEffectiveToughness(gd, messenger);
        Card yanlingCreature = new GrizzlyBears();
        yanlingCreature.setSubtypes(List.of(CardSubtype.YANLING));

        harness.addToBattlefield(player1, yanlingCreature);

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Loses the boost when the Yanling planeswalker leaves")
    void losesBoostWhenYanlingLeaves() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        int basePower = gqs.getEffectivePower(gd, messenger);
        int baseToughness = gqs.getEffectiveToughness(gd, messenger);
        harness.addToBattlefield(player1, new MuYanlingCelestialWind());
        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(baseToughness + 1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.YANLING));

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(baseToughness);
    }

}
