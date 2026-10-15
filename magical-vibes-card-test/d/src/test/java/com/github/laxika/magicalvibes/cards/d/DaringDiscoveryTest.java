package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LaeliaTheBladeReforged;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringDiscovery.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class,
        LaeliaTheBladeReforged.class, Plains.class})
class DaringDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents up to three target creatures from blocking")
    void preventsUpToThreeCreaturesFromBlocking() {
        var creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var creature3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains(), new HillGiant()));
        cast(List.of(creature1.getId(), creature2.getId(), creature3.getId()));

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
        assertThat(creature3.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can be cast without choosing creatures and discovers a card into hand")
    void canChooseNoCreaturesAndPutDiscoveredCardIntoHand() {
        GrizzlyBears discovered = new GrizzlyBears();
        Plains land = new Plains();
        CrawWurm tooExpensive = new CrawWurm();
        harness.setLibrary(player1, List.of(land, tooExpensive, discovered));
        cast(List.of());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Plains land = new Plains();
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(new DaringDiscovery()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("Casts a discovered card with mana value exactly four without paying mana")
    void castsDiscoveredCardForFree() {
        HillGiant discovered = new HillGiant();
        Plains skipped = new Plains();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(skipped, discovered, remaining));
        cast(List.of());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(discovered));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, skipped);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Returns all skipped cards when no discover hit exists")
    void noQualifyingCardReturnsEntireLibrary() {
        Plains land = new Plains();
        CrawWurm expensive = new CrawWurm();
        harness.setLibrary(player1, List.of(land, expensive));
        cast(List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Daring Discovery");
    }

    @Test
    @DisplayName("Does not discover when every chosen target has left the battlefield")
    void allTargetsGonePreventsDiscover() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new DaringDiscovery()));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(discovered);
        harness.assertInGraveyard(player1, "Daring Discovery");
    }

    @Test
    @DisplayName("Still prevents blocking and discovers when one chosen target remains")
    void resolvesWithOneRemainingTarget() {
        var removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new DaringDiscovery()));
        addMana();
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(remaining.isCantBlockThisTurn()).isTrue();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    @DisplayName("Discover exiles each skipped card and the hit separately, triggering Laelia")
    void discoverTriggersLaeliaForEveryExiledCard() {
        var laelia = harness.addToBattlefieldAndReturn(player1, new LaeliaTheBladeReforged());
        Plains land = new Plains();
        CrawWurm expensive = new CrawWurm();
        HillGiant discovered = new HillGiant();
        harness.setLibrary(player1, List.of(land, expensive, discovered));
        cast(List.of());
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land, expensive, discovered);
    }

    private void cast(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new DaringDiscovery()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
