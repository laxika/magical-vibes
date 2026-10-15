package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniSleeperAgent;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsNemesis;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunlitHoplite.class, ElspethSunsNemesis.class, AjaniSleeperAgent.class})
class SunlitHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllerTurn() {
        Permanent hoplite = addCreatureReady(player1, new SunlitHoplite());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, hoplite, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, hoplite, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 while its controller controls an Elspeth planeswalker")
    void getsBoostWhileControllerControlsElspethPlaneswalker() {
        Permanent hoplite = addCreatureReady(player1, new SunlitHoplite());

        assertThat(gqs.getEffectivePower(gd, hoplite)).isEqualTo(2);
        harness.addToBattlefield(player1, new ElspethSunsNemesis());
        assertThat(gqs.getEffectivePower(gd, hoplite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hoplite)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Elspeth planeswalker does not provide the boost")
    void opponentElspethDoesNotProvideBoost() {
        Permanent hoplite = addCreatureReady(player1, new SunlitHoplite());

        harness.addToBattlefield(player2, new ElspethSunsNemesis());

        assertThat(gqs.getEffectivePower(gd, hoplite)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Elspeth planeswalker does not provide the boost")
    void nonElspethPlaneswalkerDoesNotProvideBoost() {
        Permanent hoplite = addCreatureReady(player1, new SunlitHoplite());

        harness.addToBattlefield(player1, new AjaniSleeperAgent());

        assertThat(gqs.getEffectivePower(gd, hoplite)).isEqualTo(2);
    }
}
