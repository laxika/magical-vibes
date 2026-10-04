package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fertilid.class, GrizzlyBears.class, Plains.class, Forest.class, Island.class, Mutavault.class})
class FertilidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new Fertilid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(fertilid(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating removes a +1/+1 counter and target player fetches a basic land tapped")
    void activateFetchesBasicLand() {
        Permanent fertilid = readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibraryWithBasicLands(player2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(fertilid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND));

        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
        long tappedLands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                .count();
        assertThat(tappedLands).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Controller may target themselves")
    void mayTargetSelf() {
        readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibraryWithBasicLands(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
    }

    @Test
    @DisplayName("Target player may fail to find")
    void mayFailToFind() {
        readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibraryWithBasicLands(player2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No basic lands in library — no search prompt")
    void noBasicLands() {
        readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gameLogContains("finds no basic land cards")).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with no +1/+1 counters to remove")
    void cannotActivateWithoutCounters() {
        Permanent fertilid = addCreatureReady(player1, new Fertilid());
        fertilid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while summoning sick and tapped")
    void canActivateWhileSummoningSickAndTapped() {
        harness.setHand(player1, List.of(new Fertilid()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent fertilid = fertilid(player1);
        assertThat(fertilid.isSummoningSick()).isTrue();
        fertilid.setTapped(true);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(fertilid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(fertilid.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing the last counter kills Fertilid but its ability still resolves")
    void lastCounterAbilityResolvesAfterSourceDies() {
        Permanent fertilid = readyFertilid(player1);
        fertilid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(fertilid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fertilid");
        harness.assertInGraveyard(player1, "Fertilid");
        harness.handleCardChosen(player2, 0);
        assertThat(findPermanent(player2, "Plains").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Search excludes nonbasic lands and nonland cards")
    void excludesNonbasicLandsAndCreatures() {
        readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Forest forest = new Forest();
        Mutavault mutavault = new Mutavault();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(mutavault, bears, forest));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(mutavault, bears);
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("An empty library still consumes the activation cost")
    void emptyLibraryStillConsumesCounter() {
        Permanent fertilid = readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(fertilid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("searches their library but it is empty")).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the ability's mana cost without green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent fertilid = readyFertilid(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fertilid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        Permanent fertilid = readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fertilid.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fertilid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent fertilid(Player player) {
        return findPermanent(player, "Fertilid");
    }

    private Permanent readyFertilid(Player player) {
        Permanent fertilid = addCreatureReady(player, new Fertilid());
        fertilid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        return fertilid;
    }

    private void setupLibraryWithBasicLands(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
