package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class HibernationsEndTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep searches for a creature with the matching mana value")
    void payingCumulativeUpkeepSearchesForMatchingCreature() {
        Permanent hibernationsEnd = harness.addToBattlefieldAndReturn(player1, new HibernationsEnd());
        hibernationsEnd.setCounterCount(CounterType.AGE, 1);
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name").containsExactly("Grizzly Bears");

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Hibernation's End")
    void decliningCumulativeUpkeepSacrificesHibernationsEnd() {
        Permanent hibernationsEnd = harness.addToBattlefieldAndReturn(player1, new HibernationsEnd());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hibernationsEnd);
        harness.assertInGraveyard(player1, "Hibernation's End");
    }
}
