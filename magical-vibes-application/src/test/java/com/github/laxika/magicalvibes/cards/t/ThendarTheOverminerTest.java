package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThendarTheOverminer.class, Wastes.class, FieldOfRuin.class, Mountain.class,
        StoneRain.class, GrizzlyBears.class, ImprisonedInTheMoon.class})
class ThendarTheOverminerTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures a tapped Wastes for a nonbasic land's controller")
    void conjuresTappedWastesForNonbasicLandController() {
        harness.addToBattlefield(player1, new ThendarTheOverminer());
        UUID fieldOfRuinId = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin()).getId();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, fieldOfRuinId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent wastes = findPermanent(player2, "Wastes");
        assertThat(wastes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not conjure for a basic land")
    void doesNotConjureForBasicLand() {
        harness.addToBattlefield(player1, new ThendarTheOverminer());
        UUID mountainId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, mountainId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Wastes");
    }

    @Test
    @DisplayName("Destroys up to one nonbasic land when two creatures are tapped")
    void destroysNonbasicLandAtEndStep() {
        harness.addToBattlefield(player1, new ThendarTheOverminer());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstCreature.tap();
        secondCreature.tap();
        UUID fieldOfRuinId = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(fieldOfRuinId);
        harness.handlePermanentChosen(player1, fieldOfRuinId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Field of Ruin");
    }

    @Test
    @DisplayName("Does not trigger with fewer than two tapped creatures")
    void doesNotTriggerWithFewerThanTwoTappedCreatures() {
        harness.addToBattlefield(player1, new ThendarTheOverminer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.addToBattlefield(player2, new FieldOfRuin());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Field of Ruin");
    }

    @Test
    @DisplayName("Rechecks the tapped creature condition when the end-step ability resolves")
    void doesNotDestroyIfTappedCreatureCountDrops() {
        Permanent thendar = harness.addToBattlefieldAndReturn(player1, new ThendarTheOverminer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        thendar.tap();
        bears.tap();
        UUID landId = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, landId);
        bears.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Field of Ruin");
        harness.assertNotOnBattlefield(player2, "Wastes");
    }

    @Test
    @DisplayName("Can choose no land even when a legal target exists")
    void canChooseZeroTargets() {
        Permanent thendar = harness.addToBattlefieldAndReturn(player1, new ThendarTheOverminer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        thendar.tap();
        bears.tap();
        UUID landId = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin()).getId();
        UUID basicId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(landId, player1.getId()).doesNotContain(basicId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Field of Ruin");
        harness.assertNotOnBattlefield(player2, "Wastes");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent thendar = harness.addToBattlefieldAndReturn(player1, new ThendarTheOverminer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        thendar.tap();
        bears.tap();
        harness.addToBattlefield(player2, new FieldOfRuin());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Field of Ruin");
    }

    @Test
    @DisplayName("Conjures Wastes when a creature turned into a nonbasic land dies")
    void conjuresForCreatureTurnedIntoLand() {
        harness.addToBattlefield(player1, new ThendarTheOverminer());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new ImprisonedInTheMoon(), new StoneRain()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Wastes").isTapped()).isTrue();
    }
}
