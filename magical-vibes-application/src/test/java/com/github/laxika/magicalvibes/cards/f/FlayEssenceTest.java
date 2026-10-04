package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GristTheHungerTide;
import com.github.laxika.magicalvibes.cards.k.KitchenImp;
import com.github.laxika.magicalvibes.cards.m.MistyRainforest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlayEssence.class, KitchenImp.class, GristTheHungerTide.class, MistyRainforest.class})
class FlayEssenceTest extends BaseCardTest {

    @Test
    void exilesCreatureAndGainsLifeForAllCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitchenImp());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player1, 10);

        castFlayEssence(target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    void canExilePlaneswalkerAndGainLifeForItsLoyaltyCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GristTheHungerTide());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player1, 10);

        castFlayEssence(target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistyRainforest());
        harness.setHand(player1, List.of(new FlayEssence()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    private void castFlayEssence(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FlayEssence()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    void exilesCreatureWithoutCountersWithoutGainingLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitchenImp());
        harness.setLife(player1, 10);

        castFlayEssence(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertLife(player1, 10);
    }

    @Test
    void countsCountersAtResolutionRatherThanWhenCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitchenImp());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.setHand(player1, List.of(new FlayEssence()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.setCounterCount(CounterType.CHARGE, 2);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 12);
    }

    @Test
    void gainsNoLifeWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitchenImp());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 10);
        FlayEssence spell = new FlayEssence();
        harness.setHand(player1, List.of(spell));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void canExileOwnCreatureAndGainLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KitchenImp());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);

        castFlayEssence(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.assertLife(player1, 12);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
