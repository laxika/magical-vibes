package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddedSails.class, AirElemental.class, GrizzlyBears.class, Millstone.class})
class ShreddedSailsTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void dealsFourDamageToTargetCreatureWithFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void rejectsIllegalTargetsForEachMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingDiscardsTheCardAndDrawsOne() {
        harness.setHand(player1, List.of(new ShreddedSails()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shredded Sails");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
