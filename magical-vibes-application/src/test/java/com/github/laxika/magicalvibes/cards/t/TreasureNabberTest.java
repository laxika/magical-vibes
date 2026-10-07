package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureNabber.class, SolRing.class, Forest.class, LiquimetalCoating.class, EverflowingChalice.class})
class TreasureNabberTest extends BaseCardTest {

    @Test
    void gainsControlOfAnOpponentsArtifactTappedForManaUntilEndOfYourNextTurn() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent solRing = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(solRing.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(solRing.getId()).duration())
                .isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.UNTIL_END_OF_YOUR_NEXT_TURN);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Sol Ring");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player2, "Sol Ring");
    }

    @Test
    void doesNotTriggerForAnOpponentsNonartifactManaPermanent() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.newestControlEffectFor(forest.getId())).isNull();
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void doesNotTriggerWhenYouTapYourOwnArtifactForMana() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
        assertThat(gd.newestControlEffectFor(solRing.getId())).isNull();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void gainsControlOfALandMadeIntoAnArtifactWhenTappedForMana() {
        harness.addToBattlefield(player1, new TreasureNabber());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 1, null, forest.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, forest)).isTrue();

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void tappingAnArtifactForANonManaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent coating = harness.addToBattlefieldAndReturn(player2, new LiquimetalCoating());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player2, 0, null, forest.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Liquimetal Coating");
        assertThat(coating.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(coating.getId())).isNull();
    }

    @Test
    void gainsControlEvenWhenTheTappedManaAbilityProducesZeroMana() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent chalice = harness.addToBattlefieldAndReturn(player2, new EverflowingChalice());

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Everflowing Chalice");
        harness.assertNotOnBattlefield(player2, "Everflowing Chalice");
        assertThat(chalice.isTapped()).isTrue();
    }

    @Test
    void controlGainedDuringOpponentsTurnExpiresAfterYourImmediatelyFollowingTurn() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new TreasureNabber());
        harness.addToBattlefield(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Sol Ring");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Sol Ring");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player2, "Sol Ring");
        harness.assertNotOnBattlefield(player1, "Sol Ring");
    }
}
