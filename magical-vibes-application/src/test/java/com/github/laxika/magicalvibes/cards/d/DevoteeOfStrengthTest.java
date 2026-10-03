package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
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

@CardUsed({DevoteeOfStrength.class, FeralProwler.class})
class DevoteeOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupDevotee();
        UUID targetId = harness.getPermanentId(player1, "Feral Prowler");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Feral Prowler");
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not tap the devotee when activated")
    void doesNotTapOnActivation() {
        setupDevotee();
        UUID targetId = harness.getPermanentId(player1, "Feral Prowler");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Devotee of Strength").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupDevotee();
        UUID targetId = harness.getPermanentId(player1, "Feral Prowler");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Feral Prowler");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void canTargetItselfWhileSummoningSickAndTapped() {
        setupDevotee();
        Permanent devotee = findPermanent(player1, "Devotee of Strength");
        devotee.setSummoningSick(true);
        devotee.setTapped(true);

        harness.activateAbility(player1, 0, null, devotee.getId());
        harness.passBothPriorities();

        assertThat(devotee.getPowerModifier()).isEqualTo(2);
        assertThat(devotee.getToughnessModifier()).isEqualTo(2);
        assertThat(devotee.isTapped()).isTrue();
    }

    @Test
    void canBoostAnOpponentsCreature() {
        setupDevotee();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FeralProwler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(findPermanent(player1, "Feral Prowler").getPowerModifier()).isZero();
    }

    @Test
    void repeatedActivationsStackAndExpireTogether() {
        setupDevotee();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent target = findPermanent(player1, "Feral Prowler");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void cannotActivateAgainWithoutPayingAnotherFiveMana() {
        setupDevotee();
        Permanent target = findPermanent(player1, "Feral Prowler");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isEqualTo(2);
    }

    private void setupDevotee() {
        harness.addToBattlefield(player1, new DevoteeOfStrength());
        harness.addToBattlefield(player1, new FeralProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
