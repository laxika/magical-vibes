package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.h.HatchetBully;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoofSkulkin.class, NettleSentinel.class, HatchetBully.class})
class HoofSkulkinTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a green creature +1/+1 until end of turn, then it wears off")
    void boostsGreenCreature() {
        harness.addToBattlefield(player1, new HoofSkulkin());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new NettleSentinel()); // 2/2 green
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Nettle Sentinel");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(greenCreature.getEffectivePower()).isEqualTo(3);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(greenCreature.getEffectivePower()).isEqualTo(2);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        harness.addToBattlefield(player1, new HoofSkulkin());
        harness.addToBattlefield(player1, new HatchetBully()); // red
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Hatchet Bully");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can boost an opposing green creature")
    void boostsOpposingCreature() {
        harness.addToBattlefield(player1, new HoofSkulkin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NettleSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void repeatedActivationsStackWithoutTapping() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new HoofSkulkin());
        skulkin.setSummoningSick(true);
        skulkin.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(skulkin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with less than three mana")
    void requiresThreeMana() {
        harness.addToBattlefield(player1, new HoofSkulkin());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NettleSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target the colorless Hoof Skulkin itself")
    void cannotTargetColorlessCreature() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new HoofSkulkin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, skulkin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
