package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeikoYamazakiTheGeneral.class, MothriderSamurai.class, Ornithopter.class,
        Shock.class, GrizzlyBears.class})
class HeikoYamazakiTheGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("A lone Samurai may cast an artifact from the graveyard this turn")
    void loneSamuraiMayCastArtifactFromGraveyard() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new MothriderSamurai());
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter, new Shock()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromGraveyard(player1, ornithopter.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("The trigger does not fire for a non-Samurai, non-Warrior attacker")
    void triggerDoesNotFireForOtherCreature() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Ornithopter()));

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does not fire when a Samurai or Warrior attacks with another creature")
    void triggerDoesNotFireWhenAttackingWithAnotherCreature() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Ornithopter()));

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No permission is granted when there is no artifact card to target")
    void noPermissionWithoutArtifactTarget() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new MothriderSamurai());
        harness.setGraveyard(player1, List.of(new Shock()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("Declining the trigger does not grant permission to cast from the graveyard")
    void decliningTriggerDoesNotGrantPermission() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new MothriderSamurai());
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
    }
}
