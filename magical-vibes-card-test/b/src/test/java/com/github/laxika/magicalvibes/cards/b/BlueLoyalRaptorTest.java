package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HuntingVelociraptor;
import com.github.laxika.magicalvibes.cards.o.OwenGradyRaptorTrainer;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlueLoyalRaptor.class, HuntingVelociraptor.class, OwenGradyRaptorTrainer.class, TurnToFrog.class})
class BlueLoyalRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Each other Dinosaur enters with one counter of every kind on Blue")
    void copiesCounterKindsToOtherDinosaurs() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        blue.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        blue.setCounterCount(CounterType.CHARGE, 2);

        Permanent raptor = harness.enterBattlefieldAndReturn(player1, new HuntingVelociraptor());

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(raptor.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blue does not add counters to non-Dinosaurs or opposing creatures")
    void onlyAffectsControlledDinosaurs() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        blue.setCounterCount(CounterType.CHARGE, 1);

        Permanent owen = harness.enterBattlefieldAndReturn(player1, new OwenGradyRaptorTrainer());
        Permanent opposingRaptor = harness.enterBattlefieldAndReturn(player2, new HuntingVelociraptor());

        assertThat(owen.getCounters()).isEmpty();
        assertThat(opposingRaptor.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("Partner with lets the targeted player search for Owen Grady")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card owen = new OwenGradyRaptorTrainer();
        harness.setLibrary(player2, List.of(owen));
        harness.castFromHand(player1, new BlueLoyalRaptor(), "{2}{G}{U}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(owen);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(owen);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(owen);
    }

    @Test
    @DisplayName("A Dinosaur enters without additional counters when Blue has none")
    void noCountersOnBlueAddsNoCounters() {
        harness.addToBattlefield(player1, new BlueLoyalRaptor());

        Permanent raptor = harness.enterBattlefieldAndReturn(player1, new HuntingVelociraptor());

        assertThat(raptor.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("Entering Dinosaurs use the counter kinds currently on Blue")
    void usesCurrentCounterKinds() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        blue.setCounterCount(CounterType.HASTE, 2);
        Permanent first = harness.enterBattlefieldAndReturn(player1, new HuntingVelociraptor());

        blue.setCounterCount(CounterType.HASTE, 0);
        blue.setCounterCount(CounterType.TRAMPLE, 3);
        Permanent second = harness.enterBattlefieldAndReturn(player1, new HuntingVelociraptor());

        assertThat(first.getCounterCount(CounterType.HASTE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(second.getCounterCount(CounterType.HASTE)).isZero();
        assertThat(second.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blue cannot give entering Dinosaurs counters after losing all abilities")
    @CardUsed({BlueLoyalRaptor.class, HuntingVelociraptor.class, TurnToFrog.class})
    void losesAbilitiesStopsEntryReplacement() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        blue.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, blue.getId());

        Permanent raptor = harness.enterBattlefieldAndReturn(player1, new HuntingVelociraptor());

        assertThat(raptor.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("The targeted player may decline the partner search")
    void targetedPlayerMayDeclineSearch() {
        Card owen = new OwenGradyRaptorTrainer();
        harness.setLibrary(player2, List.of(owen));
        harness.castFromHand(player1, new BlueLoyalRaptor(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(owen);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(owen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blue's controller may target themselves with the partner search")
    void controllerMaySearchOwnLibrary() {
        Card owen = new OwenGradyRaptorTrainer();
        harness.setLibrary(player1, List.of(owen));
        harness.castFromHand(player1, new BlueLoyalRaptor(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(owen);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(owen);
    }

    @Test
    @DisplayName("The targeted player may fail to find Owen even when he is in their library")
    void mayFailToFindPartner() {
        Card owen = new OwenGradyRaptorTrainer();
        harness.setLibrary(player2, List.of(owen));
        harness.castFromHand(player1, new BlueLoyalRaptor(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(owen);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(owen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
