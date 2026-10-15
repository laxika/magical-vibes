package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitaniasCommand.class, Forest.class, Island.class, GrizzlyBears.class, Shock.class})
class TitaniasCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard mode exiles all cards and gains life for each card exiled")
    void exilesGraveyardAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Shock(), new Island())));
        cast(new int[]{0, 2}, List.of(player2.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 23);
        harness.assertOnBattlefield(player1, "Bear");
    }

    @Test
    @DisplayName("Land mode searches for up to two lands and puts them onto the battlefield tapped")
    void searchesForTwoTappedLands() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TitaniasCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2}, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        harness.assertOnBattlefield(player1, "Bear");
    }

    @Test
    @DisplayName("Counter mode puts two +1/+1 counters on each creature you control")
    void putsTwoCountersOnEachControlledCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(new int[]{2, 3}, List.of());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Graveyard mode rejects a permanent target")
    void graveyardModeRequiresPlayerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TitaniasCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 2, new int[]{0, 2}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bear mode resolves before counters even when selected in reverse order")
    void newlyCreatedBearsReceiveCountersInPrintedModeOrder() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(new int[]{3, 2}, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bear"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
                    assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(4);
                });
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An empty graveyard remains a legal target and gains no life")
    void emptyGraveyardDoesNotPreventBearMode() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of());

        cast(new int[]{0, 2}, List.of(player2.getId()));

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bear"))
                .hasSize(2);
    }

    @Test
    @DisplayName("The controller can exile their own graveyard")
    void canTargetOwnGraveyard() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new Forest(), new Island()));

        cast(new int[]{0, 2}, List.of(player1.getId()));

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Titania's Command");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Land search can decline to find lands and continues to the Bear mode")
    void canFindZeroLands() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        cast(new int[]{1, 2}, List.of());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(p -> p.getCard().getName().equals("Bear"));
    }

    @Test
    @DisplayName("Land search can stop after finding one land")
    void canFindOneLand() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        cast(new int[]{1, 2}, List.of());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Bear");
    }

    @Test
    @DisplayName("Both searched lands are selected before either enters the battlefield")
    void searchedLandsEnterTogether() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        cast(new int[]{1, 2}, List.of());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        harness.assertOnBattlefield(player1, "Bear");
    }
    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new TitaniasCommand()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
