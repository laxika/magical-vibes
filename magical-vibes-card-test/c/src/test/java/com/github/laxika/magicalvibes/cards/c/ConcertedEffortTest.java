package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.d.DesertNomads;
import com.github.laxika.magicalvibes.cards.d.DimirHouseGuard;
import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.r.ReaverTitan;
import com.github.laxika.magicalvibes.cards.s.Sewerdreg;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.w.WeatherseedFaeries;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConcertedEffort.class, BorosRecruit.class, BorosSwiftblade.class, CourierHawk.class,
        SiegeWurm.class, WeatherseedFaeries.class, DesertNomads.class, GuardianOfTheGuildpact.class,
        DimirHouseGuard.class, Sewerdreg.class, ReaverTitan.class})
class ConcertedEffortTest extends BaseCardTest {

    private void resolveUpkeepTrigger(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Shares keywords present among creatures you control at upkeep")
    void sharesKeywords() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent firstStrike = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent doubleStrike = harness.addToBattlefieldAndReturn(player1, new BorosSwiftblade());
        Permanent flyingAndVigilance = harness.addToBattlefieldAndReturn(player1, new CourierHawk());
        Permanent trample = harness.addToBattlefieldAndReturn(player1, new SiegeWurm());

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasKeyword(gd, firstStrike, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstStrike, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, doubleStrike, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, trample, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, flyingAndVigilance, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Shares protection abilities from creatures you control")
    void sharesProtection() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent faeries = harness.addToBattlefieldAndReturn(player1, new WeatherseedFaeries());

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, faeries, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Shared abilities wear off at end of turn")
    void sharedAbilitiesWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addToBattlefield(player1, new CourierHawk());

        resolveUpkeepTrigger(player1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Checks shared keywords when the upkeep trigger resolves")
    void checksSharedKeywordsAtResolution() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new CourierHawk());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Triggers during each player's upkeep")
    void triggersDuringEachPlayersUpkeep() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addToBattlefield(player1, new CourierHawk());

        resolveUpkeepTrigger(player2);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Shares desertwalk as a landwalk ability")
    void sharesDesertwalk() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent desertwalker = harness.addToBattlefieldAndReturn(player1, new DesertNomads());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DESERTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, desertwalker, Keyword.DESERTWALK)).isTrue();
    }

    @Test
    @DisplayName("Shares protection from monocolored sources")
    void sharesProtectionFromMonocolored() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfTheGuildpact());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasProtectionFromSource(gd, recruit, guardian)).isTrue();
    }

    @Test
    void sharesFearAndSwampwalk() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addToBattlefield(player1, new DimirHouseGuard());
        harness.addToBattlefield(player1, new Sewerdreg());

        resolveUpkeepTrigger(player1);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.SWAMPWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    void doesNotShareOpponentsAbilitiesOrGrantToOpponents() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent opponentHawk = harness.addToBattlefieldAndReturn(player2, new CourierHawk());

        resolveUpkeepTrigger(player2);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHawk, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGainSharedAbilities() {
        harness.addToBattlefield(player1, new ConcertedEffort());
        harness.addToBattlefield(player1, new CourierHawk());
        harness.addToBattlefield(player1, new WeatherseedFaeries());

        resolveUpkeepTrigger(player1);
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, recruit, CardColor.RED)).isFalse();
    }

    @Test
    void sharesProtectionFromLowManaValuesFromCrewedVehicle() {
        gd.turnNumber = 2;
        java.util.Set<TurnStep> stops = java.util.Set.of(
                TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN, TurnStep.UPKEEP);
        gd.playerAutoStopSteps.put(player1.getId(), stops);
        gd.playerAutoStopSteps.put(player2.getId(), stops);
        harness.addToBattlefield(player1, new ConcertedEffort());
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new ReaverTitan());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SiegeWurm());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new CourierHawk());
        Permanent opposingWurm = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, titan)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, titan, hawk)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, recruit, hawk)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, recruit, opposingWurm)).isFalse();
    }
}
