package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WickedWolf.class, GildedGoose.class, GrizzlyBears.class})
class WickedWolfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB fights up to one target creature an opponent controls")
    void entersAndFightsTargetCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent wolf = castWolf();

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing a Food puts a counter on Wicked Wolf, grants indestructible, and taps it")
    void sacrificesFoodForCounterIndestructibleAndTap() {
        Permanent wolf = castGooseAndWolf();

        harness.activateAbility(player1, battlefieldIndex(player1, wolf), null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Wicked Wolf's activated ability loses indestructible at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent wolf = castGooseAndWolf();

        harness.activateAbility(player1, battlefieldIndex(player1, wolf), null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castWolf() {
        harness.setHand(player1, List.of(new WickedWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Wicked Wolf");
    }

    private Permanent castGooseAndWolf() {
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return castWolf();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
