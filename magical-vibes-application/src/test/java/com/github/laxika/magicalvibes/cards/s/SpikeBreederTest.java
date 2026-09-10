package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoxDiamond;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeBreeder.class, WallOfBlossoms.class, MoxDiamond.class})
class SpikeBreederTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.castFromHand(player1, new SpikeBreeder(), "{3}{G}");
        harness.passBothPriorities();

        Permanent breeder = findPermanent(player1, "Spike Breeder");
        assertThat(breeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, breeder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, breeder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a counter to put one on target creature")
    void putsCounterOnTargetCreature() {
        Permanent breeder = readyBreeder();
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfBlossoms());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(breeder), 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(breeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can put a counter on an opponent's creature")
    void putsCounterOnOpponentsCreature() {
        Permanent breeder = readyBreeder();
        Permanent opponentWall = harness.addToBattlefieldAndReturn(player2, new WallOfBlossoms());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(breeder), 0, null, opponentWall.getId());
        harness.passBothPriorities();

        assertThat(breeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentWall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter to create a 1/1 green Spike token")
    void createsSpikeToken() {
        Permanent breeder = readyBreeder();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(breeder), 1, null, null);
        harness.passBothPriorities();

        assertThat(breeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Spike")
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getColor() == CardColor.GREEN
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIKE));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent breeder = readyBreeder();
        Permanent moxDiamond = harness.addToBattlefieldAndReturn(player1, new MoxDiamond());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(breeder), 0, null, moxDiamond.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent breeder = readyBreeder();
        breeder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(breeder), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent readyBreeder() {
        Permanent breeder = addCreatureReady(player1, new SpikeBreeder());
        breeder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        return breeder;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
