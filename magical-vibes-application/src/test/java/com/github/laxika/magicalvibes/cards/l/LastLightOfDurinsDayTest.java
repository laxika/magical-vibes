package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastLightOfDurinsDay.class, DragonWhelp.class, Mountain.class, Forest.class})
class LastLightOfDurinsDayTest extends BaseCardTest {

    @Test
    @DisplayName("A Mountain adds the sixth quest counter, sacrifices the enchantment, and searches hand or library")
    void mountainReachesSixCountersAndFindsDragon() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        DragonWhelp dragon = new DragonWhelp();
        harness.setHand(player1, List.of(new Mountain(), dragon));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchHandAndOrLibraryChoice.class);
        harness.assertInGraveyard(player1, "Last Light of Durin's Day");

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == dragon);
    }

    @Test
    @DisplayName("A non-Mountain land does not add a quest counter")
    void nonMountainDoesNotTrigger() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(light.getCounterCount(CounterType.QUEST)).isZero();
        harness.assertOnBattlefield(player1, "Last Light of Durin's Day");
    }

    @Test
    @DisplayName("A Dragon in the graveyard is not eligible for the search")
    void graveyardDragonIsNotEligible() {
        Permanent light = harness.addToBattlefieldAndReturn(player1, new LastLightOfDurinsDay());
        light.setCounterCount(CounterType.QUEST, 5);
        DragonWhelp dragon = new DragonWhelp();
        harness.setGraveyard(player1, List.of(dragon));
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Dragon Whelp");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == dragon);
    }

    @Test
    @DisplayName("Mountaincycling discards this card and offers only Mountains")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new LastLightOfDurinsDay()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Last Light of Durin's Day");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(mountain);
    }
}
