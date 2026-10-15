package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({SpikeTiller.class, Forest.class, AshcoatBear.class})
class SpikeTillerTest extends BaseCardTest {

    @Test
    @DisplayName("Spike Tiller enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        Permanent tiller = castTiller();

        assertThat(tiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, tiller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tiller)).isEqualTo(3);
    }

    @Test
    @DisplayName("First ability removes a counter from Spike Tiller and adds one to target creature")
    void addsCounterToTargetCreature() {
        Permanent tiller = addReadyTiller(player1);
        Permanent bears = addReadyCreature(player1, new AshcoatBear());
        tiller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(tiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability permanently animates a land and puts a counter on it")
    void animatesLandAndAddsCounter() {
        Permanent tiller = addReadyTiller(player1);
        Permanent land = addReadyLand(player1);
        tiller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    @DisplayName("Abilities cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        addReadyTiller(player1);
        Permanent bears = addReadyCreature(player1, new AshcoatBear());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Land ability rejects a nonland creature target")
    void landAbilityRequiresLandTarget() {
        Permanent tiller = addReadyTiller(player1);
        Permanent bears = addReadyCreature(player1, new AshcoatBear());
        tiller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter transfer resolves even when paying the last counter kills Spike Tiller")
    void transfersLastCounterToOpponentsCreature() {
        Permanent tiller = castTiller();
        Permanent bear = addReadyCreature(player2, new AshcoatBear());
        tiller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tiller);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tiller.getCard());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated animation accumulates counters and survives the source dying")
    void repeatedlyAnimatesOpponentsLand() {
        Permanent tiller = castTiller();
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        for (int activation = 0; activation < 3; activation++) {
            harness.activateAbility(player1, 0, 1, null, land.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tiller);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
    }

    @Test
    @DisplayName("Spike Tiller can transfer a counter to itself without tapping")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent tiller = castTiller();
        tiller.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, tiller.getId());
        assertThat(tiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(tiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(tiller.isTapped()).isTrue();
    }

    private Permanent castTiller() {
        harness.setHand(player1, List.of(new SpikeTiller()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Spike Tiller");
    }

    private Permanent addReadyTiller(Player player) {
        return addReadyCreature(player, new SpikeTiller());
    }

    private Permanent addReadyLand(Player player) {
        return addReadyCreature(player, new Forest());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
