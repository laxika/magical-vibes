package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TapestryWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SusurianDirgecraft.class, GrizzlyBears.class, TapestryWarden.class})
class SusurianDirgecraftTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent sacrifices a nontoken creature")
    void entersAndEachOpponentSacrificesNontokenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDirgecraft();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Station puts charge counters equal to the tapped creature's power on it")
    void stationUsesTappedCreaturePowerAtResolution() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Seven charge counters make it a flying artifact creature")
    void sevenChargeCountersUnlockCreatureAndFlying() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());

        assertThat(gqs.isCreature(gd, dirgecraft)).isFalse();
        assertThat(gqs.hasKeyword(gd, dirgecraft, Keyword.FLYING)).isFalse();

        dirgecraft.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, dirgecraft)).isTrue();
        assertThat(gqs.hasKeyword(gd, dirgecraft, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationRequiresAnotherUntappedCreature() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringDoesNotSacrificeControllersCreatureOrOpponentToken() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new TapestryWarden());
        TapestryWarden tokenCard = new TapestryWarden();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new TapestryWarden());

        castDirgecraft();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token).doesNotContain(opponent);
        harness.assertInGraveyard(player2, "Tapestry Warden");
    }

    @Test
    void opponentChoosesWhichNontokenCreatureToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new TapestryWarden());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TapestryWarden());

        castDirgecraft();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void enteringWithNoOpponentCreaturesStillResolves() {
        castDirgecraft();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Susurian Dirgecraft");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreatureCanStationUsingToughnessWithWarden() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new TapestryWarden());
        warden.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null);
        assertThat(warden.isTapped()).isTrue();
        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();

        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void animatedDirgecraftCannotStationItself() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        dirgecraft.setCounterCount(CounterType.CHARGE, 7);
        dirgecraft.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dirgecraft.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotStation() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new TapestryWarden());
        warden.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCreatureCannotPayStationCost() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        harness.addToBattlefield(player2, new TapestryWarden());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCannotBeActivatedOutsideMainPhase() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        harness.addToBattlefield(player1, new TapestryWarden());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCannotBeActivatedWithSpellOnStack() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        harness.addToBattlefield(player1, new TapestryWarden());
        castDirgecraft();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingChargeCountersRemovesCreatureStatusAndFlying() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        dirgecraft.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, dirgecraft)).isTrue();
        assertThat(gqs.hasKeyword(gd, dirgecraft, Keyword.FLYING)).isTrue();

        dirgecraft.setCounterCount(CounterType.CHARGE, 6);

        assertThat(gqs.isCreature(gd, dirgecraft)).isFalse();
        assertThat(gqs.hasKeyword(gd, dirgecraft, Keyword.FLYING)).isFalse();
    }

    @Test
    void stationUsesLastKnownPowerWhenTappedCreatureLeaves() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void creatureTokenCanPayStationCost() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        TapestryWarden tokenCard = new TapestryWarden();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null);
        harness.passBothPriorities();

        assertThat(token.isTapped()).isTrue();
        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void zeroPowerCreatureStationsWithoutAddingCounters() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPowerModifier(-2);

        harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(dirgecraft.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedDuringOpponentsTurn() {
        Permanent dirgecraft = harness.addToBattlefieldAndReturn(player1, new SusurianDirgecraft());
        harness.addToBattlefield(player1, new TapestryWarden());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dirgecraft), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDirgecraft() {
        harness.castFromHand(player1, new SusurianDirgecraft(), "{4}{B}");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
