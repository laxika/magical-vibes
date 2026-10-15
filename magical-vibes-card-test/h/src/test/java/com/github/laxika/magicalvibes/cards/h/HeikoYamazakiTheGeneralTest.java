package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronApprentice;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnstoppableOgre;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        Shock.class, GrizzlyBears.class, IronApprentice.class, UnstoppableOgre.class})
class HeikoYamazakiTheGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("A lone Samurai may cast an artifact from the graveyard this turn")
    void loneSamuraiMayCastArtifactFromGraveyard() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new MothriderSamurai());
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter, new Shock()));

        declareAttackers(player1, List.of(1));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, ornithopter.getId());
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

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.graveyardPlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Declining the trigger does not grant permission to cast from the graveyard")
    void decliningTriggerDoesNotGrantPermission() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new MothriderSamurai());
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        declareAttackers(player1, List.of(1));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
    }

    @Test
    @DisplayName("Heiko's own lone attack grants permission but does not waive artifact mana costs")
    void ownLoneAttackRequiresNormalCastingCosts() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        Card artifact = new IronApprentice();
        harness.setGraveyard(player1, List.of(artifact));

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, artifact.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Iron Apprentice");
        harness.assertNotInGraveyard(player1, "Iron Apprentice");
    }

    @Test
    @DisplayName("A lone Warrior also grants permission to cast the targeted artifact")
    void loneWarriorGrantsPermission() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player1, new UnstoppableOgre());
        Card artifact = new IronApprentice();
        harness.setGraveyard(player1, List.of(artifact));

        declareAttackers(List.of(1));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, artifact.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Iron Apprentice");
    }

    @Test
    @DisplayName("An opponent's lone Warrior does not trigger Heiko")
    void opponentsLoneWarriorDoesNotTrigger() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        addCreatureReady(player2, new UnstoppableOgre());
        harness.setGraveyard(player1, List.of(new IronApprentice()));

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.graveyardPlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Artifacts in an opponent's graveyard are not legal targets")
    void opponentsGraveyardDoesNotProvideTarget() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        harness.setGraveyard(player2, List.of(new IronApprentice()));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.graveyardPlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Permission applies only to the targeted artifact and does not allow casting during combat")
    void permissionIsSpecificAndPreservesNormalTiming() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        Card target = new IronApprentice();
        Card other = new IronApprentice();
        harness.setGraveyard(player1, List.of(target, other));

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, other.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
        harness.castFromGraveyard(player1, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Iron Apprentice");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("The permission expires when the turn ends")
    void permissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new HeikoYamazakiTheGeneral());
        Card artifact = new IronApprentice();
        harness.setGraveyard(player1, List.of(artifact));

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
    }
}
