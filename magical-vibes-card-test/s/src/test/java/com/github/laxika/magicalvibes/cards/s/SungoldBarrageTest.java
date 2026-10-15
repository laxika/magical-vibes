package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SungoldBarrage.class, AirElemental.class, GrizzlyBears.class})
class SungoldBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with toughness 4 or greater")
    void destroysToughCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castSungoldBarrage(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Rejects a creature with toughness less than 4")
    void rejectsLowToughnessCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new SungoldBarrage()));
        addSungoldBarrageMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 4 or greater");
    }

    @Test
    @DisplayName("Can destroy its controller's creature")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castSungoldBarrage(target);

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Uses modified toughness when choosing a target")
    void destroysCreatureBoostedToFourToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setToughnessModifier(2);
        castSungoldBarrage(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a creature reduced to three toughness")
    void rejectsCreatureReducedBelowThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setToughnessModifier(-1);
        harness.setHand(player1, java.util.List.of(new SungoldBarrage()));
        addSungoldBarrageMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 4 or greater");
    }

    @Test
    @DisplayName("Does not destroy a target whose toughness falls below four before resolution")
    void rechecksToughnessOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, java.util.List.of(new SungoldBarrage()));
        addSungoldBarrageMana();
        harness.castInstant(player1, 0, target.getId());
        target.setToughnessModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Sungold Barrage");
    }

    private void castSungoldBarrage(Permanent target) {
        harness.setHand(player1, java.util.List.of(new SungoldBarrage()));
        addSungoldBarrageMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addSungoldBarrageMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
