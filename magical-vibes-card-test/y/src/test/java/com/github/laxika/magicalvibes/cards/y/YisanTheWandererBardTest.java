package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Ulcerate;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YisanTheWandererBard.class, LlanowarElves.class, GrizzlyBears.class, HillGiant.class, Ulcerate.class})
class YisanTheWandererBardTest extends BaseCardTest {

    private Permanent setUpYisan() {
        harness.addToBattlefield(player1, new YisanTheWandererBard());
        Permanent yisan = findPermanent(player1, "Yisan, the Wanderer Bard");
        yisan.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears(), new HillGiant()));
        return yisan;
    }

    @Test
    @DisplayName("The verse counter is put on as a cost and bounds the search to that mana value")
    void firstActivationFindsManaValueOne() {
        Permanent yisan = setUpYisan();

        harness.activateAbility(player1, 0, null, null);
        assertThat(yisan.getCounterCount(CounterType.VERSE)).isEqualTo(1);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Llanowar Elves"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("A second activation raises the counter to two and finds a mana value 2 creature")
    void secondActivationFindsManaValueTwo() {
        Permanent yisan = setUpYisan();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        yisan.untap();
        harness.activateAbility(player1, 0, null, null);
        assertThat(yisan.getCounterCount(CounterType.VERSE)).isEqualTo(2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Grizzly Bears"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No creature with the matching mana value means nothing is found")
    void noMatchingManaValueFindsNothing() {
        harness.addToBattlefield(player1, new YisanTheWandererBard());
        findPermanent(player1, "Yisan, the Wanderer Bard").setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The search uses the verse counter count at resolution")
    void counterCountIsReadAtResolution() {
        Permanent yisan = setUpYisan();
        harness.activateAbility(player1, 0, null, null);
        yisan.setCounterCount(CounterType.VERSE, 2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(c -> c.getName()).containsExactly("Grizzly Bears");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Yisan leaving the battlefield preserves its last verse counter count")
    void usesLastKnownCountersAfterYisanDies() {
        Permanent yisan = setUpYisan();
        harness.activateAbility(player1, 0, null, null);
        yisan.setCounterCount(CounterType.VERSE, 2);
        harness.setHand(player2, List.of(new Ulcerate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, yisan.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Yisan, the Wanderer Bard");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(c -> c.getName()).containsExactly("Grizzly Bears");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A restricted library search may fail to find an available creature")
    void mayDeclineToFindMatchingCreature() {
        Permanent yisan = setUpYisan();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(yisan.getCounterCount(CounterType.VERSE)).isEqualTo(1);
        assertThat(yisan.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Searching an empty library still resolves after paying the cost")
    void emptyLibraryResolvesWithoutAChoice() {
        Permanent yisan = setUpYisan();
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        assertThat(yisan.getCounterCount(CounterType.VERSE)).isEqualTo(1);
        assertThat(yisan.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature card with the matching mana value cannot be found")
    void onlyMatchingCreaturesCanBeChosen() {
        setUpYisan();
        harness.setLibrary(player1, List.of(new Ulcerate(), new LlanowarElves()));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(c -> c.getName()).containsExactly("Llanowar Elves");
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(findPermanent(player1, "Llanowar Elves").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(c -> c.getName()).containsExactly("Ulcerate");
    }

    @Test
    @DisplayName("A summoning-sick Yisan cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent yisan = setUpYisan();
        yisan.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(yisan.getCounterCount(CounterType.VERSE)).isZero();
        assertThat(yisan.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Yisan cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent yisan = setUpYisan();
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(yisan.getCounterCount(CounterType.VERSE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }
}
