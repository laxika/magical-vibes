package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.t.ThrivingIbex;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GearshiftAce.class, RenegadeFreighter.class, ThrivingIbex.class, ChandrasPyrohelix.class})
class GearshiftAceTest extends BaseCardTest {

    @Test
    @DisplayName("When Gearshift Ace crews a Vehicle, it gains first strike until end of turn")
    void vehicleGainsFirstStrikeWhenAceCrewsIt() {
        Permanent ace = addCreatureReady(player1, new GearshiftAce());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.isCreature(gd, freighter)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(ace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Gearshift Ace does not trigger when another creature crews the Vehicle")
    void vehicleDoesNotGainFirstStrikeWhenAnotherCreatureCrewsIt() {
        addCreatureReady(player1, new GearshiftAce());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        Permanent ibex = addCreatureReady(player1, new ThrivingIbex());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        harness.handlePermanentChosen(player1, ibex.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The granted first strike expires at end of turn")
    void grantedFirstStrikeExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GearshiftAce());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void summoningSickAceCanCrewAndGrantFirstStrike() {
        Permanent ace = harness.addToBattlefieldAndReturn(player1, new GearshiftAce());
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        resolveAllTriggers();

        assertThat(ace.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void onlyTheCrewedVehicleGainsFirstStrike() {
        addCreatureReady(player1, new GearshiftAce());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        Permanent otherVehicle = addCreatureReady(player1, new RenegadeFreighter());
        Permanent opposingVehicle = addCreatureReady(player2, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherVehicle, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingVehicle, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void removingAceDoesNotStopItsPendingTrigger() {
        Permanent ace = addCreatureReady(player1, new GearshiftAce());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        harness.castInstant(player2, 0, Map.of(ace.getId(), 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gearshift Ace");
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
