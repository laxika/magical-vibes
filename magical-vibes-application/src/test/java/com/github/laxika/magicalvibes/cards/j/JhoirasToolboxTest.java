package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DefenseGrid;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.TickingGnomes;
import com.github.laxika.magicalvibes.cards.v.ViashinoHeretic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhoirasToolbox.class, TickingGnomes.class, GiantCockroach.class, DefenseGrid.class,
        ViashinoHeretic.class})
class JhoirasToolboxTest extends BaseCardTest {

    @Test
    void regeneratesTargetArtifactCreature() {
        harness.addToBattlefield(player1, new JhoirasToolbox());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TickingGnomes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesTargetFromDestruction() {
        harness.addToBattlefield(player1, new JhoirasToolbox());
        addCreatureReady(player1, new ViashinoHeretic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TickingGnomes());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ticking Gnomes");
        harness.assertNotInGraveyard(player2, "Ticking Gnomes");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new JhoirasToolbox());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    void cannotTargetNonCreatureArtifact() {
        harness.addToBattlefield(player1, new JhoirasToolbox());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DefenseGrid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }
}
