package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyfireKirinTest extends BaseCardTest {

    @Test
    @DisplayName("May gain control of a creature with the triggering Arcane spell's mana value")
    void gainsControlOfExactManaValueCreature() {
        Permanent bears = prepareArcaneCast();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bears.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(
                harness.getPermanentId(player2, "Llanowar Elves"));

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may ability leaves the creature under its owner's control")
    void decliningLeavesCreatureAlone() {
        prepareArcaneCast();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Spirit, non-Arcane spell does not trigger")
    void nonMatchingSpellDoesNotTrigger() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SkyfireKirin());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    private Permanent prepareArcaneCast() {
        harness.addToBattlefield(player1, new SkyfireKirin());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new LlanowarElves());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        return bears;
    }
}
