package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.g.GrindingStation;
import com.github.laxika.magicalvibes.cards.r.RiverSong;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IdrisSoulOfTheTARDIS.class, ConjurersBauble.class, GrindingStation.class,
        RiverSong.class, SolRing.class})
class IdrisSoulOfTheTARDISTest extends BaseCardTest {

    @Test
    void exilesArtifactAndGetsItsManaValueAndActivatedAbility() {
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
        castIdris(bauble.getId());

        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        assertThat(findPermanents(player1, "Conjurer's Bauble")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, idris)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, idris)).isEqualTo(4);

        idris.setSummoningSick(false);
        var graveyardCard = new ConjurersBauble();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Idris, Soul of the TARDIS")).isEmpty();
        assertThat(findPermanents(player1, "Conjurer's Bauble")).hasSize(1);
    }

    @Test
    void gainsTriggeredAbilityOfExiledArtifact() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        station.tap();
        castIdris(station.getId());
        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        idris.tap();

        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(idris.isTapped()).isFalse();
    }

    @Test
    void canDeclineTheInheritedUntapTrigger() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        castIdris(station.getId());
        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        idris.tap();

        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(idris.isTapped()).isTrue();
    }

    @Test
    void rejectsNonArtifactChoiceDuringResolution() {
        Permanent river = harness.addToBattlefieldAndReturn(player1, new RiverSong());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.castFromHand(player1, new IdrisSoulOfTheTARDIS(), "{1}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, river.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ring.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Sol Ring")).isEmpty();
        assertThat(findPermanents(player1, "River Song")).hasSize(1);
    }

    @Test
    void canEnterWithoutAnArtifactToExile() {
        harness.addToBattlefield(player2, new SolRing());
        harness.castFromHand(player1, new IdrisSoulOfTheTARDIS(), "{1}{U}{R}");
        resolveAllTriggers();

        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        assertThat(gqs.getEffectivePower(gd, idris)).isEqualTo(3);
        assertThat(findPermanents(player2, "Sol Ring")).hasSize(1);
    }

    @Test
    void entersWithThreeTimeCountersBeforeImprintResolves() {
        harness.addToBattlefield(player1, new SolRing());
        harness.castFromHand(player1, new IdrisSoulOfTheTARDIS(), "{1}{U}{R}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Idris, Soul of the TARDIS")
                .getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void vanishesOnThirdUpkeepAndReturnsTheArtifact() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        castIdris(ring.getId());
        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(idris.getCounterCount(CounterType.TIME)).isEqualTo(2);
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(idris.getCounterCount(CounterType.TIME)).isEqualTo(2);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(idris.getCounterCount(CounterType.TIME)).isEqualTo(1);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Idris, Soul of the TARDIS")).isEmpty();
        harness.assertInGraveyard(player1, "Idris, Soul of the TARDIS");
        assertThat(findPermanents(player1, "Sol Ring")).hasSize(1);
    }

    @Test
    void losesBonusWhenTheImprintedCardLeavesExile() {
        SolRing card = new SolRing();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, card);
        castIdris(ring.getId());
        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        assertThat(gqs.getEffectivePower(gd, idris)).isEqualTo(4);

        gd.removeFromExile(card.getId());
        harness.setGraveyard(player1, List.of(card));

        assertThat(gqs.getEffectivePower(gd, idris)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, idris)).isEqualTo(3);
    }

    @Test
    void exilingAnArtifactTokenDoesNotGrantItsManaValueBonus() {
        SolRing token = new SolRing();
        token.setToken(true);
        Permanent ring = harness.addToBattlefieldAndReturn(player1, token);
        castIdris(ring.getId());
        harness.runStateBasedActions();

        Permanent idris = findPermanent(player1, "Idris, Soul of the TARDIS");
        assertThat(findPermanents(player1, "Sol Ring")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, idris)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, idris)).isEqualTo(3);
    }

    private void castIdris(java.util.UUID artifactId) {
        harness.castFromHand(player1, new IdrisSoulOfTheTARDIS(), "{1}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);
        resolveAllTriggers();
    }
}
