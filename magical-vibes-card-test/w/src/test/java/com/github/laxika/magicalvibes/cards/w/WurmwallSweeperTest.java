package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EumidianTerrabotanist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WurmwallSweeper.class, EumidianTerrabotanist.class})
class WurmwallSweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 2")
    void entersWithSurveilTwo() {
        GameData gd = harness.getGameData();
        Card top0 = new EumidianTerrabotanist();
        Card top1 = new EumidianTerrabotanist();
        gd.playerDecks.get(player1.getId()).add(0, top1);
        gd.playerDecks.get(player1.getId()).add(0, top0);

        harness.castFromHand(player1, new WurmwallSweeper(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Station uses the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        Permanent bears = addCreatureReady(player1, new EumidianTerrabotanist());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sweeper), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four charge counters make the Spacecraft an artifact creature with flying")
    void fourChargeCountersUnlockAbilities() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());

        sweeper.setCounterCount(CounterType.CHARGE, 3);
        assertThat(gqs.isCreature(gd, sweeper)).isFalse();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isFalse();

        sweeper.setCounterCount(CounterType.CHARGE, 4);
        assertThat(gqs.isCreature(gd, sweeper)).isTrue();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isTrue();
    }

    @Test
    void surveilCanPutOneCardInGraveyardAndKeepTheOtherOnTop() {
        Card first = new EumidianTerrabotanist();
        Card second = new WurmwallSweeper();
        Card third = new EumidianTerrabotanist();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new WurmwallSweeper(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    @Test
    void surveilCanKeepBothCardsInEitherOrder() {
        Card first = new EumidianTerrabotanist();
        Card second = new WurmwallSweeper();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new WurmwallSweeper(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void surveilWithOneCardCanPutItInGraveyard() {
        Card onlyCard = new EumidianTerrabotanist();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new WurmwallSweeper(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }

    @Test
    void surveilWithEmptyLibraryRequiresNoChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new WurmwallSweeper(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Wurmwall Sweeper");
    }

    @Test
    void stationCanTapSummoningSickCreatureWhileSpacecraftIsTapped() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        sweeper.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(creature.isTapped()).isTrue();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();

        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void stationCannotTapItselfOrAnOpponentsCreature() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        sweeper.setCounterCount(CounterType.CHARGE, 4);
        harness.addToBattlefield(player2, new EumidianTerrabotanist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sweeper.isTapped()).isFalse();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void stationCannotUseAnAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new WurmwallSweeper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EumidianTerrabotanist());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationWithZeroPowerAddsNoCounters() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        Permanent creature = addCreatureReady(player1, new EumidianTerrabotanist());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void repeatedStationActivationsAccumulateCountersAndUnlockFlying() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        Permanent first = addCreatureReady(player1, new EumidianTerrabotanist());
        Permanent second = addCreatureReady(player1, new EumidianTerrabotanist());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, sweeper)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(sweeper.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, sweeper)).isTrue();
        assertThat(gqs.isArtifact(gd, sweeper)).isTrue();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isTrue();
    }

    @Test
    void stationCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new WurmwallSweeper());
        Permanent creature = addCreatureReady(player1, new EumidianTerrabotanist());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void stationCannotBeActivatedWithSpellOnStack() {
        harness.addToBattlefield(player1, new WurmwallSweeper());
        Permanent creature = addCreatureReady(player1, new EumidianTerrabotanist());
        harness.castFromHand(player1, new WurmwallSweeper(), "{2}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void droppingBelowFourCountersRemovesCreatureStatusAndFlying() {
        Permanent sweeper = harness.addToBattlefieldAndReturn(player1, new WurmwallSweeper());
        sweeper.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, sweeper)).isTrue();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isTrue();

        sweeper.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.isCreature(gd, sweeper)).isFalse();
        assertThat(gqs.hasKeyword(gd, sweeper, Keyword.FLYING)).isFalse();
    }
}
