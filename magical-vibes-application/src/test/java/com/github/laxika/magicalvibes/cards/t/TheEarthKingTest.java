package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheEarthKing.class, Forest.class, GrizzlyBears.class, Plains.class, SerraAngel.class})
class TheEarthKingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 4/4 green Bear token")
    void entersAndCreatesBearToken() {
        harness.castFromHand(player1, new TheEarthKing(), "{3}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Bear");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(bear.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Searches for up to the number of high-power attacking creatures")
    void searchesForHighPowerAttackers() {
        addCreatureReady(player1, new TheEarthKing());
        Permanent highPowerAttacker = addCreatureReady(player1, new SerraAngel());
        Permanent lowPowerAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new GrizzlyBears()));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(highPowerAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(lowPowerAttacker)));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Plains");
        assertThat(search.params().remainingCount()).isEqualTo(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                .hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Snapshots the qualifying attacker count when the ability triggers")
    void snapshotsCountAtTriggerTime() {
        addCreatureReady(player1, new TheEarthKing());
        Permanent highPowerAttacker = addCreatureReady(player1, new SerraAngel());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(highPowerAttacker)));
        gd.playerBattlefields.get(player1.getId()).remove(highPowerAttacker);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when no attacking creature has power 4 or greater")
    void doesNotTriggerForLowPowerAttackers() {
        addCreatureReady(player1, new TheEarthKing());
        Permanent lowPowerAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(lowPowerAttacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
