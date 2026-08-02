package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YisanTheWandererBardTest extends BaseCardTest {

    private Permanent setUpYisan() {
        harness.addToBattlefield(player1, new YisanTheWandererBard());
        Permanent yisan = findPermanent(player1, "Yisan, the Wanderer Bard");
        yisan.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 9);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new LlanowarElves(), new GrizzlyBears(), new HillGiant()));
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

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("A second activation raises the counter to two and finds a mana value 2 creature")
    void secondActivationFindsManaValueTwo() {
        Permanent yisan = setUpYisan();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        yisan.untap();
        harness.activateAbility(player1, 0, null, null);
        assertThat(yisan.getCounterCount(CounterType.VERSE)).isEqualTo(2);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Grizzly Bears"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No creature with the matching mana value means nothing is found")
    void noMatchingManaValueFindsNothing() {
        harness.addToBattlefield(player1, new YisanTheWandererBard());
        findPermanent(player1, "Yisan, the Wanderer Bard").setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }
}
