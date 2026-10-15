package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BomatBazaarBarge;
import com.github.laxika.magicalvibes.cards.d.DemolitionStomper;
import com.github.laxika.magicalvibes.cards.f.FuturistSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MechtitanCore;
import com.github.laxika.magicalvibes.cards.r.RaidersKarve;
import com.github.laxika.magicalvibes.cards.r.ReckonerBankbuster;
import com.github.laxika.magicalvibes.cards.i.InspiritFlagshipVessel;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.cards.v.VoltageSurge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentalPilot.class, BomatBazaarBarge.class, RaidersKarve.class,
        DemolitionStomper.class, FuturistSentinel.class, MechtitanCore.class,
        ReckonerBankbuster.class, GrizzlyBears.class, VoltageSurge.class,
        TrainedArynx.class, InspiritFlagshipVessel.class})
class ExperimentalPilotTest extends BaseCardTest {

    @Test
    void paysManaAndDiscardsTwoCardsToDraftFromItsSpellbook() {
        harness.addToBattlefield(player1, new ExperimentalPilot());
        Card firstDiscard = new GrizzlyBears();
        Card secondDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
    }

    @Test
    void crewsAVehicleAsThoughItsPowerWereTwoGreater() {
        Permanent sentinel = addCreatureReady(player1, new FuturistSentinel());
        Permanent pilot = addCreatureReady(player1, new ExperimentalPilot());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @CardUsed({VoltageSurge.class})
    void wardCountersAnOpponentsSpellWhenTheyCannotPay() {
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new ExperimentalPilot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VoltageSurge()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, pilot.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pilot);
        harness.assertInGraveyard(player2, "Voltage Surge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({TrainedArynx.class})
    void crewBonusDoesNotHelpPaySaddleCosts() {
        addCreatureReady(player1, new TrainedArynx());
        Permanent pilot = addCreatureReady(player1, new ExperimentalPilot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pilot.isTapped()).isFalse();
    }

    @Test
    @CardUsed({InspiritFlagshipVessel.class})
    void crewBonusDoesNotIncreaseStationCounters() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new InspiritFlagshipVessel());
        Permanent pilot = addCreatureReady(player1, new ExperimentalPilot());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pilot.isTapped()).isTrue();
        assertThat(vessel.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void cannotDraftWithOnlyOneCardToDiscard() {
        harness.addToBattlefield(player1, new ExperimentalPilot());
        Card discard = new ExperimentalPilot();
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDraftWithoutBlueMana() {
        harness.addToBattlefield(player1, new ExperimentalPilot());
        Card first = new ExperimentalPilot();
        Card second = new ExperimentalPilot();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickPilotCanCrewWithoutChangingItsActualPower() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new FuturistSentinel());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new ExperimentalPilot());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }
}
