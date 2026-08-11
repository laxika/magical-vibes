package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiantfallTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode makes your creature deal its power to an opponent's creature")
    void dealsPowerDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The first mode cannot target a creature you do not control as the source")
    void sourceMustBeControlled() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sourceId = harness.getPermanentId(player2, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second mode destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, 1, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The second mode cannot target a creature")
    void destroyModeRequiresArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Giantfall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
