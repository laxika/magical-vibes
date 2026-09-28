package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VitalityHunter.class, GrizzlyBears.class})
class VitalityHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity X puts lifelink counters on up to X target creatures")
    void monstrosityUsesPaidXForCountersAndLifelinkTargets() {
        Permanent hunter = addReadyHunter();
        Permanent ownBear = addReadyCreature(player1);
        Permanent opposingBear = addReadyCreature(player2);
        addMonstrosityMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.handlePermanentChosen(player1, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hunter.isMonstrous()).isTrue();
        assertThat(ownBear.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.LIFELINK)).isTrue();
    }

    private Permanent addReadyHunter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new VitalityHunter());
        hunter.setSummoningSick(false);
        return hunter;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void addMonstrosityMana(int x) {
        harness.addMana(player1, ManaColor.COLORLESS, x);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
