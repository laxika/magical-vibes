package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.w.WeldingSparks;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SpeedwayFanatic.class, RenegadeFreighter.class, BastionMastodon.class, WeldingSparks.class})
class SpeedwayFanaticTest extends BaseCardTest {

    @Test
    @DisplayName("When Speedway Fanatic crews a Vehicle, it gains haste until end of turn")
    void vehicleGainsHasteWhenFanaticCrewsIt() {
        Permanent fanatic = addCreatureReady(player1, new SpeedwayFanatic());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isTrue();

        harness.passBothPriorities();

        assertThat(fanatic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Speedway Fanatic does not trigger when another creature crews the Vehicle")
    void vehicleDoesNotGainHasteWhenAnotherCreatureCrewsIt() {
        addCreatureReady(player1, new SpeedwayFanatic());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        harness.handlePermanentChosen(player1, mastodon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The granted haste expires at end of turn")
    void grantedHasteExpiresAtEndOfTurn() {
        addCreatureReady(player1, new SpeedwayFanatic());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Granted haste lets a newly entered Vehicle attack after crew resolves")
    void newlyEnteredVehicleCanAttackAfterFanaticCrewsIt() {
        harness.addToBattlefield(player1, new SpeedwayFanatic());
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new RenegadeFreighter());
        freighter.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, freighter)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(als.canAttack(gd, freighter, player1.getId())).isTrue();
        declareAttackers(List.of(indexOf(player1, freighter)));
        assertThat(freighter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Vehicle gains haste even if Speedway Fanatic dies before its trigger resolves")
    void triggerSurvivesFanaticLeavingBattlefield() {
        Permanent fanatic = addCreatureReady(player1, new SpeedwayFanatic());
        Permanent freighter = addCreatureReady(player1, new RenegadeFreighter());
        harness.setHand(player2, List.of(new WeldingSparks()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.activateAbility(player1, indexOf(player1, freighter), null, null);
        harness.castInstant(player2, 0, fanatic.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Speedway Fanatic");
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, freighter, Keyword.HASTE)).isTrue();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
