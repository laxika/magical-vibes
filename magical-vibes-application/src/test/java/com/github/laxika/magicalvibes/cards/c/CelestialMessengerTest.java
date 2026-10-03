package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.MuYanlingCelestialWind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialMessenger.class, MuYanlingCelestialWind.class, GreenwoodSentinel.class})
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
    @DisplayName("Loses the boost when the opponent gains control of Yanling")
    void losesBoostWhenYanlingChangesController() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        Permanent yanling = harness.addToBattlefieldAndReturn(player1, new MuYanlingCelestialWind());
        int boostedPower = gqs.getEffectivePower(gd, messenger);
        int boostedToughness = gqs.getEffectiveToughness(gd, messenger);

        gd.playerBattlefields.get(player1.getId()).remove(yanling);
        gd.playerBattlefields.get(player2.getId()).add(yanling);

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(boostedPower - 1);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(boostedToughness - 1);
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
        Card yanlingCreature = new GreenwoodSentinel();
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

    @Test
    @DisplayName("The conditional boost applies only to Messenger")
    void doesNotBoostOtherCreatures() {
        Permanent messenger = addCreatureReady(player1, new CelestialMessenger());
        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());
        int messengerPower = gqs.getEffectivePower(gd, messenger);
        int messengerToughness = gqs.getEffectiveToughness(gd, messenger);
        int sentinelPower = gqs.getEffectivePower(gd, sentinel);
        int sentinelToughness = gqs.getEffectiveToughness(gd, sentinel);

        harness.addToBattlefield(player1, new MuYanlingCelestialWind());

        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(messengerPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(messengerToughness + 1);
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(sentinelPower);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(sentinelToughness);
    }
}
