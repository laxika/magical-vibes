package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KianneCorruptedMemory.class, BurnishedHart.class, NightsWhisper.class, KenrithsTransformation.class})
class KianneCorruptedMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("At even power, Kianne grants flash to noncreature spells")
    void evenPowerGrantsNoncreatureFlash() {
        harness.addToBattlefield(player1, new KianneCorruptedMemory());
        prepareOpponentTurn();

        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("At even power, Kianne does not grant flash to creature spells")
    void evenPowerDoesNotGrantCreatureFlash() {
        harness.addToBattlefield(player1, new KianneCorruptedMemory());
        prepareOpponentTurn();

        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Kianne")
    void drawingPutsCounterOnKianne() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At odd power, Kianne grants flash to creature spells")
    void oddPowerGrantsCreatureFlash() {
        harness.addToBattlefield(player1, new KianneCorruptedMemory());
        advanceToDraw(player1);
        harness.passBothPriorities();
        prepareOpponentTurn();

        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void oddPowerDoesNotGrantNoncreatureFlash() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());
        kianne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareOpponentTurn();
        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opponentDrawDoesNotPutCounterOnKianne() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());
        harness.setLibrary(player2, List.of(new BurnishedHart()));

        advanceToDraw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void drawDoesNotChangeParityUntilTriggerResolves() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());
        harness.setLibrary(player1, List.of(new BurnishedHart()));
        advanceToDraw(player1);
        harness.setHand(player1, List.of(new BurnishedHart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void drawingTwoCardsCreatesTwoSeparateCounterTriggers() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());
        harness.setLibrary(player1, List.of(new BurnishedHart(), new BurnishedHart()));
        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        prepareOpponentTurn();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.setHand(player1, List.of(new NightsWhisper(), new BurnishedHart()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void losingAbilitiesStopsDrawCounterTriggers() {
        Permanent kianne = harness.addToBattlefieldAndReturn(player1, new KianneCorruptedMemory());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new KenrithsTransformation()));
        harness.setLibrary(player2, List.of(new BurnishedHart()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castEnchantment(player2, 0, kianne.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, kianne)).isTrue();
        harness.setLibrary(player1, List.of(new BurnishedHart()));

        advanceToDraw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(kianne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
