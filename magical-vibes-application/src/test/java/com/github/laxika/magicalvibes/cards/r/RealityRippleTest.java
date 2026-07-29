package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealityRippleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving phases out the target creature")
    void phasesOutTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Resolving phases out the target artifact")
    void phasesOutTargetArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        UUID targetId = harness.getPermanentId(player2, "Millstone");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gqs.findPermanentById(gd, targetId)).isNull();
    }

    @Test
    @DisplayName("Resolving phases out the target land")
    void phasesOutTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gqs.findPermanentById(gd, targetId)).isNull();
    }

    @Test
    @DisplayName("A phased-out permanent is not put into a graveyard — it returns during its controller's next untap step")
    void phasesBackInOnControllersNextUntapStep() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castRealityRipple(targetId);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        advanceTurn(); // player2's untap step — the creature phases in
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.phasedOutPermanents.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can phase out a permanent you control")
    void canPhaseOutOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        castRealityRipple(targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enchantment = new Permanent(new AngelicChorus());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new RealityRipple()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    private void castRealityRipple(UUID targetId) {
        harness.setHand(player1, List.of(new RealityRipple()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
