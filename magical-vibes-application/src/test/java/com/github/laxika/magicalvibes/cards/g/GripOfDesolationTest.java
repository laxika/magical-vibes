package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GripOfDesolation.class, DryadArbor.class, Forest.class, BroodhunterWurm.class})
class GripOfDesolationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target creature and target land")
    void exilesTargetCreatureAndLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareGrip();
        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), land.getId()));

        harness.assertNotOnBattlefield(player2, "Broodhunter Wurm");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(creature.getCard(), land.getCard());
    }

    @Test
    @DisplayName("Allows the same land creature to fill both target positions")
    void allowsSharedLandCreatureTarget() {
        Permanent landCreature = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        prepareGrip();
        harness.castAndResolveInstant(player1, 0, List.of(landCreature.getId(), landCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Dryad Arbor");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(landCreature.getCard());
    }

    @Test
    @DisplayName("Rejects targets in the wrong positions")
    void rejectsWrongTargetTypes() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm()).getId();
        prepareGrip();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(landId, creatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void exilesLandWhenCreatureTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareGrip();
        harness.castInstant(player1, 0, List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land.getCard());
        harness.assertInGraveyard(player1, "Grip of Desolation");
    }

    @Test
    void exilesCreatureWhenLandTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareGrip();
        harness.castInstant(player1, 0, List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(land);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Broodhunter Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
    }

    @Test
    void canTargetOwnCreatureAndOpponentsLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareGrip();

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), land.getId()));

        harness.assertNotOnBattlefield(player1, "Broodhunter Wurm");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land.getCard());
    }

    @Test
    void requiresBothTargetsToCast() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        prepareGrip();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grip of Desolation");
    }

    private void prepareGrip() {
        harness.setHand(player1, List.of(new GripOfDesolation()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
