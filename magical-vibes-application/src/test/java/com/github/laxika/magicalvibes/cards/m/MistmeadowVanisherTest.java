package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistmeadowVanisher.class, GrizzlyBears.class, Island.class})
class MistmeadowVanisherTest extends BaseCardTest {

    @Test
    @DisplayName("When it becomes tapped, it can exile a legal permanent until the next end step")
    void tapsToFlickerTarget() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(vanisher);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("Does not trigger when another permanent becomes tapped")
    void anotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MistmeadowVanisher());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only nonland nontoken permanents are legal targets")
    void targetMustBeNonlandAndNontoken() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(vanisher);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, creature.getId());
    }

    @Test
    @DisplayName("Allows declining the optional target")
    void targetMayBeDeclined() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        harness.addToBattlefield(player2, new GrizzlyBears());

        tap(vanisher);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player1.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

}
