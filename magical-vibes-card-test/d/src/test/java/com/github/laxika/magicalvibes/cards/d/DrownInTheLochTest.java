package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OakenBoon;
import com.github.laxika.magicalvibes.cards.t.TuinvaleTreefolk;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownInTheLoch.class, GrizzlyBears.class, Island.class,
        TuinvaleTreefolk.class, OakenBoon.class, WalkingBallista.class})
class DrownInTheLochTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell whose controller has enough cards in their graveyard")
    void countersSpellWithinControllerGraveyardCount() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(bears));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a spell whose mana value exceeds its controller's graveyard count")
    void cannotCounterSpellAboveControllerGraveyardCount() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(bears));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a creature within its controller's graveyard count")
    void destroysCreatureWithinControllerGraveyardCount() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot use the destruction mode on a noncreature permanent")
    void cannotDestroyNoncreaturePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rechecks the graveyard count when the spell resolves")
    void rechecksGraveyardCountOnResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castInstant(player1, 0, 1, targetId);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("The caster's graveyard cannot make an opposing creature eligible")
    void cannotDestroyCreatureAboveItsControllersGraveyardCount() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setGraveyard(player2, List.of(new Island()));
        harness.setGraveyard(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own creature using your graveyard count")
    void destroysOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setGraveyard(player1, List.of(new Island(), new Island()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counter mode rechecks the target controller's graveyard on resolution")
    void counterModeRechecksGraveyardCountOnResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(bears));
        harness.setGraveyard(player2, List.of(new Island(), new Island()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, bears.getId());

        harness.setGraveyard(player2, List.of(new Island()));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Drown in the Loch");
    }

    @Test
    @DisplayName("Each X symbol contributes to a target spell's mana value")
    void cannotCounterDoubleXSpellAboveGraveyardCount() {
        WalkingBallista ballista = new WalkingBallista();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(ballista));
        harness.setGraveyard(player2, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castArtifact(player2, 0, 2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, ballista.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter mode uses the Adventure spell's mana value")
    void countersAdventureUsingAdventureManaValue() {
        TuinvaleTreefolk treefolk = new TuinvaleTreefolk();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(treefolk));
        harness.setGraveyard(player2, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        addDrownMana();

        harness.castAdventure(player2, 0, creatureId);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, treefolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tuinvale Treefolk");
        assertThat(gd.findExiledCard(treefolk.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addDrownMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
