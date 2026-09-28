package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EmrakulThePromisedEnd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TheHeronMoon.class, EmrakulThePromisedEnd.class, GrizzlyBears.class})
@DisplayName("The Heron Moon")
class TheHeronMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds colorless mana")
    void tappingAddsColorlessMana() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(moon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability exiles the bottom card of an opponent's library")
    void exilesBottomCard() {
        addCreatureReady(player1, new TheHeronMoon());
        Card top = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player2, List.of(top, bottom));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(bottom);
        Permanent moon = findPermanent(player1, "The Heron Moon");
        assertThat(moon.getCounterCount(CounterType.RELEASE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Thirteen release counters sacrifice the land and cast an Emrakul copy")
    void thirteenReleaseCountersCastEmrakulCopy() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        moon.setCounterCount(CounterType.RELEASE, 12);
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(moon);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Emrakul, the Promised End")).isNotNull();
    }
}
