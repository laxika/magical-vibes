package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HarrierNaga;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrindDust.class, HarrierNaga.class, Forest.class})
class GrindDustTest extends BaseCardTest {

    @Test
    @DisplayName("Grind puts a -1/-1 counter on its target without restricting blocking")
    void grindPutsCounterWithoutRestrictingBlocking() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        harness.setHand(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isFalse();
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
        Permanent withCounter = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        Permanent alsoCountered = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        Permanent clean = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        withCounter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        alsoCountered.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

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
        Permanent clean = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(clean.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dust requires sorcery timing")
    void dustRequiresSorceryTiming() {
        Permanent withCounter = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
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

    @Test
    @DisplayName("Grind can put one counter on each of two creatures controlled by different players")
    void grindCanTargetTwoCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new HarrierNaga());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        harness.setHand(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opposing.getId()));

        assertThat(own.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(own.isCantBlockThisTurn()).isFalse();
        assertThat(opposing.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Grind can be cast with zero targets")
    void grindCanTargetZeroCreatures() {
        GrindDust spell = new GrindDust();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Grind cannot target three creatures")
    void grindCannotTargetThreeCreatures() {
        List<Permanent> targets = IntStream.range(0, 3)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new HarrierNaga()))
                .toList();
        harness.setHand(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                targets.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dust skips a target that loses its counter while exiling remaining legal targets")
    void dustRechecksCountersOnResolution() {
        Permanent noLongerCountered = harness.addToBattlefieldAndReturn(player1, new HarrierNaga());
        Permanent stillCountered = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        noLongerCountered.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        stillCountered.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        GrindDust spell = new GrindDust();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(noLongerCountered.getId(), stillCountered.getId()));
        noLongerCountered.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .contains(noLongerCountered);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(stillCountered.getCard().getId()));
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Dust is exiled even when its only target becomes illegal")
    void dustIsExiledWhenAllTargetsBecomeIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        GrindDust spell = new GrindDust();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(target.getId()));
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(target);
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(spell.getId()));
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dust can target more than ninety-nine countered creatures")
    void dustHasNoNinetyNineTargetLimit() {
        List<Permanent> targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new HarrierNaga()))
                .toList();
        targets.forEach(p -> p.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1));
        GrindDust spell = new GrindDust();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .containsAll(targets.stream().map(Permanent::getCard).toList());
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Dust exiles a chosen friendly creature and leaves an unchosen countered creature")
    void dustExilesOnlyChosenCreatures() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new HarrierNaga());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new HarrierNaga());
        chosen.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        unchosen.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, chosen.getId());

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(chosen.getCard().getId()));
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(unchosen);
    }

    @Test
    @DisplayName("Dust cannot target a noncreature even when it has a -1/-1 counter")
    void dustCannotTargetCounteredNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setGraveyard(player1, List.of(new GrindDust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
