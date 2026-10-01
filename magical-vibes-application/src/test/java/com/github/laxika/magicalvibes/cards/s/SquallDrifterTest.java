package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquallDrifter.class, KrovikanScoundrel.class, SnowCoveredPlains.class})
class SquallDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps Squall Drifter and spends white mana")
    void activatingAbilityTapsSourceAndSpendsMana() {
        Permanent drifter = addCreatureReady(player1, new SquallDrifter());
        Permanent target = addCreatureReady(player2, new KrovikanScoundrel());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(drifter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving the ability taps the target creature")
    void resolvingAbilityTapsTargetCreature() {
        addCreatureReady(player1, new SquallDrifter());
        Permanent target = addCreatureReady(player2, new KrovikanScoundrel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature controlled by its controller")
    void canTargetCreatureControlledByItsController() {
        addCreatureReady(player1, new SquallDrifter());
        Permanent target = addCreatureReady(player1, new KrovikanScoundrel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature that is already tapped")
    void canTargetAlreadyTappedCreature() {
        addCreatureReady(player1, new SquallDrifter());
        Permanent target = addCreatureReady(player2, new KrovikanScoundrel());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new SquallDrifter());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
