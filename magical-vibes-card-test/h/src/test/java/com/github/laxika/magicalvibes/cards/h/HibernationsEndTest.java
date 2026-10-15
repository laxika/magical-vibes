package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.c.ColdsteelHeart;
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

@CardUsed({BorealCentaur.class, BorealDruid.class, ColdsteelHeart.class, HibernationsEnd.class})
class HibernationsEndTest extends BaseCardTest {

    @Test
    @DisplayName("The first upkeep adds an age counter and finds a one-mana creature")
    void firstUpkeepFindsOneManaCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HibernationsEnd());
        harness.setLibrary(player1, List.of(new BorealDruid(), new BorealCentaur()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(enchantment.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name").containsExactly("Boreal Druid");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Boreal Druid");
        assertThat(findPermanent(player1, "Boreal Druid").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting("name").containsExactly("Boreal Centaur");
    }

    @Test
    @DisplayName("A paid upkeep with no matching creature leaves the enchantment in play")
    void paidUpkeepWithNoMatchingCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HibernationsEnd());
        harness.setLibrary(player1, List.of(new BorealCentaur(), new ColdsteelHeart()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Hibernation's End")).isSameAs(enchantment);
        harness.assertNotOnBattlefield(player1, "Boreal Centaur");
        harness.assertNotOnBattlefield(player1, "Coldsteel Heart");
        assertThat(gd.playerDecks.get(player1.getId())).extracting("name")
                .containsExactlyInAnyOrder("Boreal Centaur", "Coldsteel Heart");
    }

    @Test
    @DisplayName("The creature search may fail to find even when a matching card exists")
    void mayFailToFindMatchingCreature() {
        harness.addToBattlefield(player1, new HibernationsEnd());
        harness.setLibrary(player1, List.of(new BorealDruid()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Boreal Druid");
        harness.assertOnBattlefield(player1, "Hibernation's End");
        assertThat(gd.playerDecks.get(player1.getId())).extracting("name").containsExactly("Boreal Druid");
    }

    @Test
    @DisplayName("Paying cumulative upkeep searches for a creature with the matching mana value")
    void payingCumulativeUpkeepSearchesForMatchingCreature() {
        Permanent hibernationsEnd = harness.addToBattlefieldAndReturn(player1, new HibernationsEnd());
        hibernationsEnd.setCounterCount(CounterType.AGE, 1);
        harness.setLibrary(player1, List.of(new BorealDruid(), new BorealCentaur(), new ColdsteelHeart()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name").containsExactly("Boreal Centaur");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Boreal Centaur");
        harness.assertNotOnBattlefield(player1, "Boreal Druid");
        harness.assertNotOnBattlefield(player1, "Coldsteel Heart");
    }

    @Test
    @DisplayName("Paying cumulative upkeep may decline the creature search")
    void payingCumulativeUpkeepMayDeclineCreatureSearch() {
        Permanent hibernationsEnd = harness.addToBattlefieldAndReturn(player1, new HibernationsEnd());
        harness.setLibrary(player1, List.of(new BorealCentaur()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Hibernation's End")).isSameAs(hibernationsEnd);
        harness.assertNotOnBattlefield(player1, "Boreal Centaur");
        assertThat(gd.playerDecks.get(player1.getId())).extracting("name")
                .containsExactly("Boreal Centaur");
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
