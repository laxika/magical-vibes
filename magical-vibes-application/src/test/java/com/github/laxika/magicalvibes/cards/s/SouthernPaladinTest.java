package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SouthernPaladin.class, HillGiant.class, GrizzlyBears.class, AetherFlash.class})
class SouthernPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target red permanent")
    void resolvingDestroysTargetRedPermanent() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Southern Paladin").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Resolving destroys a red noncreature permanent")
    void resolvingDestroysRedNoncreaturePermanent() {
        setupPaladin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AetherFlash());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aether Flash");
        harness.assertInGraveyard(player2, "Aether Flash");
    }

    @Test
    @DisplayName("Cannot target a non-red permanent")
    void cannotTargetNonRedPermanent() {
        setupPaladin();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red permanent");
    }

    private void setupPaladin() {
        addCreatureReady(player1, new SouthernPaladin());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
