package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConduitOfWorldsTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from the controller's graveyard")
    void playsLandFromGraveyard() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May cast a targeted nonland permanent from the graveyard during resolution")
    void castsTargetPermanentDuringResolution() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A prior spell prevents the activated ability from offering a cast")
    void priorSpellPreventsCast() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        prepareMainPhase();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(graveyardBears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A successful cast prevents later spells but not a land play")
    void successfulCastPreventsSpellsButNotLandPlay() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(bears, forest));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a land with the cast ability")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new ConduitOfWorlds());
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
