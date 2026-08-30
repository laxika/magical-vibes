package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class RimefeatherOwlTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of snow permanents on the battlefield")
    void ptEqualsSnowPermanentCount() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);
    }

    @Test
    @DisplayName("An ice counter makes the target permanent snow")
    void iceCounterMakesTargetSnow() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
        prepareSnowActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    private void prepareSnowActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private Permanent addSnowPermanent(Player player) {
        Permanent permanent = new Permanent(new Plains());
        TestCards.mutableCard(permanent).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
