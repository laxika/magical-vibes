package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.s.SerraZealot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WesternPaladin.class, SerraZealot.class, GorillaWarrior.class, GloriousAnthem.class})
class WesternPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target white creature")
    void resolvingDestroysTargetWhiteCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new SerraZealot());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Zealot");
        harness.assertInGraveyard(player2, "Serra Zealot");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new GorillaWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white noncreature permanent")
    void cannotTargetWhiteNoncreaturePermanent() {
        setupPaladin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can target a white creature controlled by its controller")
    void canTargetOwnWhiteCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player1, new SerraZealot());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Serra Zealot");
        harness.assertInGraveyard(player1, "Serra Zealot");
    }

    @Test
    @DisplayName("Activation pays two black mana and taps Western Paladin")
    void activationPaysManaAndTapsSource() {
        Permanent paladin = setupPaladin();
        Permanent target = addCreatureReady(player2, new SerraZealot());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with only one black mana")
    void cannotActivateWithOnlyOneBlackMana() {
        setupPaladin(1);
        Permanent target = addCreatureReady(player2, new SerraZealot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Western Paladin has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new WesternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        Permanent target = addCreatureReady(player2, new SerraZealot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    private Permanent setupPaladin() {
        return setupPaladin(2);
    }

    private Permanent setupPaladin(int blackMana) {
        Permanent paladin = addCreatureReady(player1, new WesternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        return paladin;
    }
}
