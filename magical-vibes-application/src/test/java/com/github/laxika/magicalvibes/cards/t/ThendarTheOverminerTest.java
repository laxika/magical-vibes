package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        StoneRain.class, GrizzlyBears.class})
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Field of Ruin");
    }
}
