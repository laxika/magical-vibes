package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadhound.class, DoomBlade.class, Forest.class, GrizzlyBears.class, Millstone.class})
class DreadhoundTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards")
    void entersAndMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.addToBattlefield(player1, new Dreadhound());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(Forest.class, Forest.class, Forest.class);
    }

    @Test
    @DisplayName("Whenever a creature dies, each opponent loses one life")
    void creatureDeathMakesEachOpponentLoseLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new Dreadhound());
        resolveAllTriggers();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each creature card put into any library's graveyard costs opponents one life")
    void creatureCardsFromOpponentsLibraryMakeEachOpponentLoseLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new Dreadhound());
        resolveAllTriggers();

        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
