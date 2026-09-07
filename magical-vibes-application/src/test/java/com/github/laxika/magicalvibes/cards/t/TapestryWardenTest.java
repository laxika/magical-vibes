package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SledgeClassSeedship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TapestryWarden.class, GiantSpider.class, GrizzlyBears.class, SledgeClassSeedship.class})
class TapestryWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with greater toughness assign combat damage equal to toughness")
    void higherToughnessCreaturesUseToughnessForCombatDamage() {
        Permanent warden = addReadyCreature(player1, new TapestryWarden());
        Permanent spider = addReadyCreature(player1, new GiantSpider());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentSpider = addReadyCreature(player2, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentSpider)).isEqualTo(2);
    }

    @Test
    @DisplayName("A qualifying creature stations using its toughness")
    void stationUsesTappedCreatureToughness() {
        Permanent warden = addReadyCreature(player1, new TapestryWarden());
        warden.tap();
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());
        Permanent spider = addReadyCreature(player1, new GiantSpider());

        harness.activateAbility(player1, battlefieldIndex(seedship), null, null);
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isTrue();
        assertThat(seedship.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature with equal toughness and power stations using its power")
    void stationUsesPowerWhenToughnessIsNotGreater() {
        Permanent warden = addReadyCreature(player1, new TapestryWarden());
        warden.tap();
        Permanent seedship = harness.addToBattlefieldAndReturn(player1, new SledgeClassSeedship());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(seedship), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(seedship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
