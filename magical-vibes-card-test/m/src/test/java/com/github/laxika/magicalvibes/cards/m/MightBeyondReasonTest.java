package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AimHigh;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NeglectedHeirloom;
import com.github.laxika.magicalvibes.cards.t.ThornhideWolves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightBeyondReason.class, ThornhideWolves.class, Forest.class, AimHigh.class, NeglectedHeirloom.class})
class MightBeyondReasonTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on target creature without delirium")
    void putsTwoCountersWithoutDelirium() {
        Permanent target = addCreature(player2);
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts three +1/+1 counters on target creature with delirium")
    void putsThreeCountersWithDelirium() {
        Permanent target = addCreature(player2);
        harness.setGraveyard(player1, deliriumCards());
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Delirium is checked at resolution")
    void deliriumCheckedAtResolution() {
        Permanent target = addCreature(player2);
        castWithoutResolving(target);
        harness.setGraveyard(player1, deliriumCards());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NeglectedHeirloom());
        harness.setHand(player1, List.of(new MightBeyondReason()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Three card types do not enable delirium and the resolving spell does not count")
    void threeTypesDoNotEnableDelirium() {
        Permanent target = addCreature(player1);
        harness.setGraveyard(player1, List.of(
                new ThornhideWolves(), new Forest(), new NeglectedHeirloom(), new Forest()));
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the caster's graveyard enables delirium")
    void opponentsDeliriumDoesNotApply() {
        Permanent target = addCreature(player2);
        harness.setGraveyard(player2, deliriumCards());
        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing delirium before resolution restores the two-counter effect")
    void losingDeliriumBeforeResolution() {
        Permanent target = addCreature(player1);
        harness.setGraveyard(player1, deliriumCards());
        castWithoutResolving(target);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that leaves before resolution receives no counters")
    void removedTargetReceivesNoCounters() {
        Permanent target = addCreature(player2);
        castWithoutResolving(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        castWithoutResolving(target);
        harness.passBothPriorities();
    }

    private void castWithoutResolving(Permanent target) {
        harness.setHand(player1, List.of(new MightBeyondReason()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Card> deliriumCards() {
        return List.of(new ThornhideWolves(), new Forest(), new AimHigh(), new NeglectedHeirloom());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThornhideWolves());
    }
}
