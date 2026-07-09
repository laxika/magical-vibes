package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

class SowerOfTemptationTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of target opponent creature")
    void etbGainsControlOfTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        castSower(bears.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Grizzly Bears now controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));

        // Tracked as source-dependent steal keyed to the Sower
        Permanent sower = findPermanent(player1, "Sower of Temptation");
        assertThat(gd.sourceDependentStolenCreatures.get(bears.getId())).isEqualTo(sower.getId());
    }

    @Test
    @DisplayName("Stolen creature returns to its owner when the Sower leaves the battlefield")
    void stolenCreatureReturnsWhenSowerBounced() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        castSower(bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sower = findPermanent(player1, "Sower of Temptation");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));

        // Bounce the Sower with Unsummon
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sower.getId());
        harness.passBothPriorities();

        // Grizzly Bears returns to player2, tracking cleaned up
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.sourceDependentStolenCreatures).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new SowerOfTemptation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSower(UUID targetId) {
        harness.setHand(player1, List.of(new SowerOfTemptation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
