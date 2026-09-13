package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicPage;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WesternPaladin.class, AngelicPage.class, HillGiant.class, GloriousAnthem.class})
class WesternPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target white creature")
    void resolvingDestroysTargetWhiteCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new AngelicPage());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Western Paladin").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Angelic Page");
        harness.assertInGraveyard(player2, "Angelic Page");
    }

    @Test
    @DisplayName("Activation pays two black mana")
    void activationPaysTwoBlackMana() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new AngelicPage());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Angelic Page");
    }

    @Test
    @DisplayName("Can target a white creature controlled by its controller")
    void canTargetOwnWhiteCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player1, new AngelicPage());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Page");
        harness.assertInGraveyard(player1, "Angelic Page");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(findPermanent(player1, "Western Paladin").isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Hill Giant");
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
    @DisplayName("Cannot activate when the Paladin is already tapped")
    void cannotActivateWhenTapped() {
        setupPaladin();
        Permanent paladin = findPermanent(player1, "Western Paladin");
        paladin.tap();
        Permanent target = addCreatureReady(player2, new AngelicPage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Angelic Page");
    }

    @Test
    @DisplayName("Cannot activate without two black mana")
    void cannotActivateWithoutTwoBlackMana() {
        Permanent paladin = addCreatureReady(player1, new WesternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent target = addCreatureReady(player2, new AngelicPage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(paladin.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Angelic Page");
    }

    private void setupPaladin() {
        addCreatureReady(player1, new WesternPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
