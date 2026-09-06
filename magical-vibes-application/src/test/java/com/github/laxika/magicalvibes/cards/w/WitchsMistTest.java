package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsMist.class, GrizzlyBears.class})
class WitchsMistTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature that was dealt damage this turn")
    void destroysDamagedCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature that was dealt damage this turn");
    }

    @Test
    @DisplayName("A regeneration shield saves the creature")
    void regenerationShieldSavesTheCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
