package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrazingGladehart;
import com.github.laxika.magicalvibes.cards.t.TurntimberRanger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissaRevane.class, NissasChosen.class, ElvishArchdruid.class,
        GrizzlyBears.class, GrazingGladehart.class, TurntimberRanger.class})
class NissaRevaneTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts Nissa's Chosen from the library onto the battlefield")
    void plusOneFindsNissasChosen() {
        Permanent nissa = addReadyNissa(player1, 2);
        Card chosen = new NissasChosen();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), chosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(chosen);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().getName().equals("Nissa's Chosen"));
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 gains 2 life for each Elf controlled")
    void plusOneGainsLifeForEachElf() {
        Permanent nissa = addReadyNissa(player1, 2);
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-7 puts any number of Elf creature cards from the library onto the battlefield")
    void minusSevenFindsAnyNumberOfElves() {
        Permanent nissa = addReadyNissa(player1, 7);
        harness.setLibrary(player1, List.of(
                new ElvishArchdruid(),
                new GrizzlyBears(),
                new ElvishArchdruid()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card ->
                card.hasType(CardType.CREATURE) && card.getSubtypes().contains(CardSubtype.ELF));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent ->
                permanent.getCard().getName().equals("Elvish Archdruid")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent nissa = harness.addToBattlefieldAndReturn(player, new NissaRevane());
        nissa.setCounterCount(CounterType.LOYALTY, loyalty);
        nissa.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nissa;
    }

    @Test
    void plusOneMayFailToFindChosenEvenWhenPresent() {
        Permanent nissa = addReadyNissa(player1, 2);
        Card chosen = new NissasChosen();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOneGainsNoLifeWithoutElves() {
        addReadyNissa(player1, 2);
        harness.addToBattlefield(player1, new GrazingGladehart());
        harness.addToBattlefield(player2, new NissasChosen());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void plusOneCountsElvesWhenItResolves() {
        addReadyNissa(player1, 2);
        harness.addToBattlefield(player1, new NissasChosen());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new NissasChosen());
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    void plusOneCompletesWhenNoChosenIsInLibrary() {
        Permanent nissa = addReadyNissa(player1, 2);
        Card nonElf = new GrazingGladehart();
        harness.setLibrary(player1, List.of(nonElf));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grazing Gladehart");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonElf);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusSevenMayChooseZeroElves() {
        addReadyNissa(player1, 8);
        Card chosen = new NissasChosen();
        harness.setLibrary(player1, List.of(chosen));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusSevenMayStopAfterChoosingOneElf() {
        addReadyNissa(player1, 8);
        Card first = new NissasChosen();
        Card second = new NissasChosen();
        Card nonElf = new GrazingGladehart();
        harness.setLibrary(player1, List.of(first, second, nonElf));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertNotOnBattlefield(player1, "Nissa's Chosen");
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Nissa's Chosen")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(second, nonElf);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusSevenElvesSeeEachOtherEnterSimultaneously() {
        addReadyNissa(player1, 8);
        harness.setLibrary(player1, List.of(new TurntimberRanger(), new TurntimberRanger()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertNotOnBattlefield(player1, "Turntimber Ranger");
        harness.handleCardChosen(player1, 0);

        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanents(player1, "Wolf")).hasSize(4);
        assertThat(findPermanents(player1, "Turntimber Ranger")).hasSize(2)
                .allSatisfy(ranger -> assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }
}
