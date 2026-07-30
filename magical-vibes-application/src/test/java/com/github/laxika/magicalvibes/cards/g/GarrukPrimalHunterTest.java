package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class GarrukPrimalHunterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a 3/3 Beast token and adds loyalty")
    void plusOneCreatesBeastToken() {
        Permanent garruk = addReadyGarruk(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        Permanent token = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .findFirst().orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 draws cards equal to the greatest power among creatures you control")
    void minusThreeDrawsGreatestPower() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 5);
        // Grizzly Bears is 2/2; Garruk's Companion is 3/2 — greatest power is 3.
        addCreature(player1, new GrizzlyBears());
        addCreature(player1, new GarruksCompanion());
        harness.setLibrary(player1, library(5));
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("-3 draws nothing when you control no creatures")
    void minusThreeDrawsNothingWithoutCreatures() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, library(5));
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("-3 ignores opponent's creatures")
    void minusThreeIgnoresOpponentCreatures() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 5);
        addCreature(player1, new GrizzlyBears());
        addCreature(player2, new GarruksCompanion());
        harness.setLibrary(player1, library(5));
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("-6 creates a 6/6 Wurm token for each land you control")
    void minusSixCreatesWurmPerLand() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 7);
        addLand(player1);
        addLand(player1);
        addLand(player1);
        addLand(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(countPermanents(player1, "Wurm")).isEqualTo(3);
        Permanent wurm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wurm"))
                .findFirst().orElseThrow();
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(6);
        assertThat(countPermanents(player2, "Wurm")).isZero();
    }

    @Test
    @DisplayName("-6 creates no tokens when you control no lands")
    void minusSixCreatesNothingWithoutLands() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wurm")).isZero();
    }

    private Permanent addReadyGarruk(Player player) {
        Permanent perm = new Permanent(new GarrukPrimalHunter());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> library(int count) {
        List<Card> cards = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> cards.add(new Forest()));
        return cards;
    }
}
