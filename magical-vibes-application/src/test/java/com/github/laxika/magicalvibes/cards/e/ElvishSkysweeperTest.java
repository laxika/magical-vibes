package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishSkysweeper.class, AirElemental.class, GrizzlyBears.class})
class ElvishSkysweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature destroys target creature with flying")
    void sacrificesCreatureAndDestroysFlyingCreature() {
        addReadySkysweeper();
        Permanent target = addCreatureReady(player2, new AirElemental());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Elvish Skysweeper");
        harness.assertInGraveyard(player1, "Elvish Skysweeper");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        addReadySkysweeper();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");

        harness.assertOnBattlefield(player1, "Elvish Skysweeper");
    }

    @Test
    @DisplayName("Cannot activate without the required mana")
    void cannotActivateWithoutMana() {
        addReadySkysweeper();
        Permanent target = addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySkysweeper() {
        return addCreatureReady(player1, new ElvishSkysweeper());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
