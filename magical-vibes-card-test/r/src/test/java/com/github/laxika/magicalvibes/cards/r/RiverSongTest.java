package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HymnOfTheFaller;
import com.github.laxika.magicalvibes.cards.k.KayaIntangibleSlayer;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({RiverSong.class, EvolvingWilds.class, GrizzlyBears.class, KayaIntangibleSlayer.class,
        HymnOfTheFaller.class, Plains.class, ThoughtReflection.class, WitchbaneOrb.class})
class RiverSongTest extends BaseCardTest {

    @Test
    @DisplayName("Draws from the bottom of its controller's library")
    void drawsFromBottomOfLibrary() {
        Card top = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, bottom));
        gd.playerHands.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new RiverSong());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Triggers when an opponent scries")
    void triggersWhenOpponentScries() {
        Permanent kaya = harness.addToBattlefieldAndReturn(player1, new KayaIntangibleSlayer());
        kaya.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player1, new RiverSong());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        Permanent riverSong = findPermanent(player1, "River Song");
        assertThat(riverSong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Triggers when an opponent surveils")
    void triggersWhenOpponentSurveils() {
        Permanent riverSong = harness.addToBattlefieldAndReturn(player1, new RiverSong());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new HymnOfTheFaller()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(riverSong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Triggers when an opponent searches their library")
    void triggersWhenOpponentSearchesLibrary() {
        Permanent riverSong = harness.addToBattlefieldAndReturn(player1, new RiverSong());
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.setLibrary(player2, List.of(new Plains(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(riverSong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Both cards in a doubled draw come from the bottom")
    void doubledDrawComesFromBottom() {
        Card top = new Plains();
        Card middle = new Plains();
        Card bottom = new Plains();
        harness.setLibrary(player1, List.of(top, middle, bottom));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new RiverSong());
        harness.addToBattlefield(player1, new ThoughtReflection());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom, middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Searching opponent's hexproof does not stop Spoilers")
    void searchTriggerDoesNotTargetOpponent() {
        Permanent riverSong = harness.addToBattlefieldAndReturn(player1, new RiverSong());
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setLibrary(player2, List.of(new Plains()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(riverSong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Controller searches do not trigger Spoilers")
    void controllerSearchDoesNotTrigger() {
        Permanent riverSong = harness.addToBattlefieldAndReturn(player1, new RiverSong());
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.setLibrary(player1, List.of(new Plains()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(riverSong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Losing all abilities disables drawing from the bottom")
    void losingAbilitiesDisablesBottomDraw() {
        Card top = new Plains();
        Card bottom = new Plains();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of());
        Permanent riverSong = harness.addToBattlefieldAndReturn(player1, new RiverSong());
        riverSong.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
    }
}
