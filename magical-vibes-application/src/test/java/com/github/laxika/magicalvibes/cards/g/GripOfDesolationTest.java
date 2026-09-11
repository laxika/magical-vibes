package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GripOfDesolation.class, DryadArbor.class, Forest.class, GrizzlyBears.class})
class GripOfDesolationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target creature and target land")
    void exilesTargetCreatureAndLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        castGrip(List.of(creature.getId(), land.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(creature.getCard(), land.getCard());
    }

    @Test
    @DisplayName("Allows the same land creature to fill both target positions")
    void allowsSharedLandCreatureTarget() {
        Permanent landCreature = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        castGrip(List.of(landCreature.getId(), landCreature.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(landCreature.getCard());
    }

    @Test
    @DisplayName("Rejects targets in the wrong positions")
    void rejectsWrongTargetTypes() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GripOfDesolation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(landId, creatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castGrip(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new GripOfDesolation()));
        addMana();
        harness.castInstant(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
