package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavePeople.class, Squire.class, TormodsCrypt.class})
class CavePeopleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Cave People +1/-2 until end of turn")
    void attackingBoostsSelf() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(1);
        assertThat(cavePeople.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The +1/-2 wears off at end of turn")
    void boostWearsOff() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(0);
        assertThat(cavePeople.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{1}{R}{R}, {T} ability grants mountainwalk to the target creature")
    void grantsMountainwalkToTarget() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new Squire());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(cavePeople.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mountainwalk wears off at end of turn")
    void mountainwalkWearsOff() {
        addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new Squire());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new CavePeople());
        Permanent crypt = addCreatureReady(player1, new TormodsCrypt());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, crypt.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The ability can target a creature an opponent controls")
    void grantsMountainwalkToOpponentsCreature() {
        addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player2, new Squire());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without {1}{R}{R}")
    void cannotActivateWithoutEnoughMana() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());
        Permanent target = addCreatureReady(player1, new Squire());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(cavePeople.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
