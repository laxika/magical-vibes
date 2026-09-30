package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimefeatherOwl.class, SnowCoveredPlains.class})
class RimefeatherOwlTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of snow permanents on the battlefield")
    void ptEqualsSnowPermanentCount() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        addNonSnowPermanent(player1);

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);
    }

    @Test
    @DisplayName("An ice counter makes the target permanent snow")
    void iceCounterMakesTargetSnow() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = addNonSnowPermanent(player1);
        prepareSnowActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    @Test
    @DisplayName("An ice counter can make an opponent's permanent snow")
    void iceCounterMakesOpponentsPermanentSnow() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = addNonSnowPermanent(player2);
        prepareSnowActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability requires snow mana")
    void requiresSnowMana() {
        harness.addToBattlefield(player1, new RimefeatherOwl());
        Permanent target = addNonSnowPermanent(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSnowActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private Permanent addSnowPermanent(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredPlains());
    }

    private Permanent addNonSnowPermanent(Player player) {
        Permanent permanent = new Permanent(new SnowCoveredPlains());
        TestCards.mutableCard(permanent).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
