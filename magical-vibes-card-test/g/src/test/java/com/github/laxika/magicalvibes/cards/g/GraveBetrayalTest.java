package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.Recover;
import com.github.laxika.magicalvibes.cards.c.CorpsejackMenace;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveBetrayal.class, GrizzlyBears.class, Shock.class, Recover.class, CorpsejackMenace.class})
class GraveBetrayalTest extends BaseCardTest {

    /** Player1 shocks player2's Grizzly Bears to death in player1's precombat main phase. */
    private void shockOpponentBearsToDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Grave Betrayal's trigger resolves, scheduling the return
    }

    private void advanceToEndStep() {
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent's dying creature returns under your control at the next end step with a +1/+1 counter")
    void returnsOpponentCreatureAtEndStepWithCounter() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();

        // Nothing happens right away — the return is delayed to the beginning of the next end step.
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("The returned creature is a black Zombie in addition to its other colors and types")
    void returnedCreatureIsBlackZombie() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getGrantedColors()).contains(CardColor.BLACK);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("A creature you control dying does not trigger Grave Betrayal")
    void ownCreatureDoesNotReturn() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();

        advanceToEndStep();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that leaves the graveyard before the end step does not return")
    void doesNotReturnIfCardLeftGraveyard() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();

        // The card is exiled from the graveyard in response — nothing is there to return.
        gd.playerGraveyards.get(player2.getId()).clear();

        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
    }

    @Test
    @DisplayName("The delayed return uses the stack and allows responses at the end step")
    void delayedReturnWaitsForResolution() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A scheduled return survives Grave Betrayal leaving the battlefield")
    void returnDoesNotRequireSourceToRemain() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();
        gd.playerBattlefields.get(player1.getId()).clear();

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A creature that leaves the graveyard and dies again is not returned by the old trigger")
    void oldReturnDoesNotFollowCardAcrossZoneChanges() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();
        UUID bearsCardId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        // Remove the enchantment so the second death cannot create a new return ability.
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new Shock()));
        harness.setHand(player2, List.of(new Recover()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, bearsCardId);
        harness.assertInHand(player2, "Grizzly Bears");

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        UUID bearsPermanentId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsPermanentId);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Corpsejack Menace doubles the counter placed by Grave Betrayal on entry")
    void returnCounterUsesCounterReplacementEffects() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        harness.addToBattlefield(player1, new CorpsejackMenace());
        shockOpponentBearsToDeath();

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("A death during an end step waits until the following turn's end step")
    void deathDuringEndStepWaitsForFollowingEndStep() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
