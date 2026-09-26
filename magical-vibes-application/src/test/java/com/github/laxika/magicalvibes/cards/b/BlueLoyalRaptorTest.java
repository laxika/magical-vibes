package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlueLoyalRaptor.class, RaptorHatchling.class, GrizzlyBears.class})
class BlueLoyalRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Each other Dinosaur enters with one counter of every kind on Blue")
    void copiesCounterKindsToOtherDinosaurs() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        blue.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        blue.setCounterCount(CounterType.CHARGE, 2);

        Permanent raptor = harness.enterBattlefieldAndReturn(player1, new RaptorHatchling());

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(raptor.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blue does not add counters to non-Dinosaurs or opposing creatures")
    void onlyAffectsControlledDinosaurs() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new BlueLoyalRaptor());
        blue.setCounterCount(CounterType.CHARGE, 1);

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingRaptor = harness.enterBattlefieldAndReturn(player2, new RaptorHatchling());

        assertThat(bears.getCounters()).isEmpty();
        assertThat(opposingRaptor.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("Partner with lets the targeted player search for Owen Grady")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card owen = new Card();
        owen.setName("Owen Grady, Raptor Trainer");
        harness.setLibrary(player2, List.of(owen));
        harness.setHand(player1, List.of(new BlueLoyalRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
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
        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player2.getId())).contains(owen);
    }
}
