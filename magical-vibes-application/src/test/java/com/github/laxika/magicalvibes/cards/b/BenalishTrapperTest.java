package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishTrapper.class, ArdentSoldier.class, Plains.class})
class BenalishTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Ability taps target creature and taps Benalish Trapper as a cost")
    void abilityTapsTargetCreature() {
        Permanent trapper = addCreatureReady(player1, new BenalishTrapper());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(trapper.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can tap a creature its controller controls")
    void canTapOwnCreature() {
        addCreatureReady(player1, new BenalishTrapper());
        Permanent target = addCreatureReady(player1, new ArdentSoldier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new BenalishTrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated without white mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BenalishTrapper());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability cannot be activated while Benalish Trapper has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new BenalishTrapper());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Ability cannot be activated while Benalish Trapper is tapped")
    void cannotActivateWhenTapped() {
        Permanent trapper = addCreatureReady(player1, new BenalishTrapper());
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        trapper.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
