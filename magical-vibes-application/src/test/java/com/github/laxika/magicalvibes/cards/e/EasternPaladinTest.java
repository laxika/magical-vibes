package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NaturesRevolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EasternPaladin.class, GrizzlyBears.class, HillGiant.class, NaturesRevolt.class})
class EasternPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target green creature")
    void resolvingDestroysTargetGreenCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eastern Paladin").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activation pays two black mana")
    void activationPaysTwoBlackMana() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target a green creature controlled by its controller")
    void canTargetOwnGreenCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNoncreaturePermanent() {
        setupPaladin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NaturesRevolt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Cannot activate when the Paladin is already tapped")
    void cannotActivateWhenTapped() {
        setupPaladin();
        Permanent paladin = findPermanent(player1, "Eastern Paladin");
        paladin.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without two black mana")
    void cannotActivateWithoutTwoBlackMana() {
        Permanent paladin = addCreatureReady(player1, new EasternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paladin.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void setupPaladin() {
        addCreatureReady(player1, new EasternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
