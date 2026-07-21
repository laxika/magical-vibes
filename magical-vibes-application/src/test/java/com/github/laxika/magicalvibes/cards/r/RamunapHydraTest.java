package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RamunapHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/3 with no Desert on battlefield or in graveyard")
    void baseStatsWithoutDesert() {
        harness.addToBattlefield(player1, new RamunapHydra());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 (4/4) when controller controls a Desert")
    void boostWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new RamunapHydra());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+1 (4/4) when there is a Desert card in the graveyard")
    void boostWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new RamunapHydra());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+2 (5/5) with Desert on battlefield and in graveyard")
    void bothBoostsStack() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new RamunapHydra());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(5);
    }

    @Test
    @DisplayName("Non-Desert permanents do not grant the battlefield boost")
    void nonDesertDoesNotBoost() {
        harness.addToBattlefield(player1, new RamunapHydra());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's Desert does not grant the battlefield boost")
    void opponentDesertDoesNotCount() {
        harness.addToBattlefield(player1, new RamunapHydra());
        harness.addToBattlefield(player2, new SunscorchedDesert());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's graveyard Desert does not grant the graveyard boost")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new RamunapHydra());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses battlefield boost when Desert leaves")
    void losesBoostWhenDesertLeaves() {
        harness.addToBattlefield(player1, new RamunapHydra());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Sunscorched Desert"));

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    @Test
    @DisplayName("Static boosts survive end-of-turn modifier reset")
    void staticBoostsSurviveEndOfTurnReset() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        harness.addToBattlefield(player1, new RamunapHydra());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        Permanent hydra = findHydra();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(5);

        hydra.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(5);
    }

    private Permanent findHydra() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ramunap Hydra"))
                .findFirst().orElseThrow();
    }
}
