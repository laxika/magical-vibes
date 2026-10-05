package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistmeadowVanisher.class, GrizzlyBears.class, Island.class, SolRing.class})
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

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
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

    @Test
    @DisplayName("Can exile itself and return untapped even after its source leaves")
    void canExileItself() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());

        tap(vanisher);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vanisher.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mistmeadow Vanisher");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Mistmeadow Vanisher");
        assertThat(returned.getId()).isNotEqualTo(vanisher.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A token copy is not a legal target")
    void tokenCannotBeTargeted() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        MistmeadowVanisher tokenCard = new MistmeadowVanisher();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        tap(vanisher);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(vanisher.getId()).doesNotContain(token.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Mistmeadow Vanisher");
    }

    @Test
    @DisplayName("A stolen permanent returns to its owner rather than its former controller")
    void returnsUnderOwnersControl() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        tap(vanisher);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, stolen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(vanisher);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mistmeadow Vanisher");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(vanisher);
    }

    @Test
    @DisplayName("An exile during an end step waits for the following turn's end step")
    void exileDuringEndStepWaitsForNextEndStep() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        tap(vanisher);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vanisher.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mistmeadow Vanisher");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Mistmeadow Vanisher");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistmeadow Vanisher");
    }

    @Test
    @DisplayName("Can exile a noncreature artifact")
    void canExileNoncreaturePermanent() {
        Permanent vanisher = harness.addToBattlefieldAndReturn(player1, new MistmeadowVanisher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        tap(vanisher);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sol Ring");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Sol Ring");
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }

}
