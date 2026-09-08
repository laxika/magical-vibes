package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfGhirapur.class, GrizzlyBears.class, ChromaticStar.class, Forest.class})
class GuardianOfGhirapurTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles another creature and returns it at the next end step")
    void flickersAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGuardian(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
    }

    @Test
    @DisplayName("ETB can exile a noncreature artifact")
    void flickersNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ChromaticStar());
        castGuardian(artifact.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chromatic Star");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Chromatic Star");
    }

    @Test
    @DisplayName("ETB may resolve without a target")
    void mayResolveWithoutTarget() {
        harness.setHand(player1, List.of(new GuardianOfGhirapur()));
        addGuardianMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guardian of Ghirapur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's permanent or a land")
    void rejectsInvalidTargets() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfGhirapur()));
        addGuardianMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGuardian(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GuardianOfGhirapur()));
        addGuardianMana();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addGuardianMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
