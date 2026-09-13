package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.cards.g.GreaterGood;
import com.github.laxika.magicalvibes.cards.g.Guma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EasternPaladin.class, BullHippo.class, Guma.class, GreaterGood.class})
class EasternPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target green creature")
    void resolvingDestroysTargetGreenCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new BullHippo());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bull Hippo");
        harness.assertInGraveyard(player2, "Bull Hippo");
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new Guma());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNoncreaturePermanent() {
        setupPaladin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterGood());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
        harness.assertOnBattlefield(player2, "Greater Good");
    }

    @Test
    @DisplayName("Can target a green creature controlled by its controller")
    void canTargetOwnGreenCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player1, new BullHippo());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bull Hippo");
        harness.assertInGraveyard(player1, "Bull Hippo");
    }

    @Test
    @DisplayName("Activation pays two black mana and taps Eastern Paladin")
    void activationPaysManaAndTapsSource() {
        Permanent paladin = setupPaladin();
        Permanent target = addCreatureReady(player2, new BullHippo());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with only one black mana")
    void cannotActivateWithOnlyOneBlackMana() {
        setupPaladin(1);
        Permanent target = addCreatureReady(player2, new BullHippo());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Eastern Paladin has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new EasternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Permanent target = addCreatureReady(player2, new BullHippo());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    private Permanent setupPaladin() {
        return setupPaladin(2);
    }

    private Permanent setupPaladin(int blackMana) {
        Permanent paladin = addCreatureReady(player1, new EasternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        return paladin;
    }
}
