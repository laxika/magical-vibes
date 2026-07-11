package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        gs.handleLibraryCardChosen(gd, player2, 0);

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
        gs.handleLibraryCardChosen(gd, player1, 0);

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
        gs.handleLibraryCardChosen(gd, player2, -1);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No basic lands in library — no search prompt")
    void noBasicLands() {
        readyFertilid(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Cannot activate with no +1/+1 counters to remove")
    void cannotActivateWithoutCounters() {
        Permanent fertilid = new Permanent(new Fertilid());
        fertilid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        fertilid.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fertilid);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent fertilid(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fertilid"))
                .findFirst().orElseThrow();
    }

    private Permanent readyFertilid(Player player) {
        Permanent fertilid = new Permanent(new Fertilid());
        fertilid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        fertilid.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(fertilid);
        return fertilid;
    }

    private void setupLibraryWithBasicLands(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
