package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrindDustTest extends BaseCardTest {

    @Test
    @DisplayName("Grind puts a -1/-1 counter on target and makes it unable to block")
    void grindPutsCounterAndCantBlock() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Grind cannot target a non-creature")
    void grindCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dust exiles creatures with -1/-1 counters, then exiles itself")
    void dustExilesCounteredCreaturesAndSelf() {
        Permanent withCounter = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent alsoCountered = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent clean = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        alsoCountered.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(withCounter.getId(), alsoCountered.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(withCounter.getId()) || p.getId().equals(alsoCountered.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(clean.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(withCounter.getCard().getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(alsoCountered.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grind") || c.getName().equals("Dust"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grind"));
    }

    @Test
    @DisplayName("Dust may exile zero creatures and still resolves, then exiles itself")
    void dustWithZeroTargetsStillResolves() {
        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grind") || c.getName().equals("Dust"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grind"));
    }

    @Test
    @DisplayName("Dust cannot target a creature without a -1/-1 counter")
    void dustCannotTargetCreatureWithoutCounter() {
        Permanent clean = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(clean.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dust requires sorcery timing")
    void dustRequiresSorceryTiming() {
        Permanent withCounter = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(withCounter.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }
}
