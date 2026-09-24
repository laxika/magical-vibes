package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaryReadAndAnneBonny.class, Island.class, TalasScout.class,
        RenegadeFreighter.class, GrizzlyBears.class})
class MaryReadAndAnneBonnyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and discards, creating a tapped Treasure for an Island")
    void islandDiscardCreatesTappedTreasure() {
        lootAndDiscard(new Island());

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Creates a tapped Treasure for a Pirate discard")
    void pirateDiscardCreatesTappedTreasure() {
        lootAndDiscard(new TalasScout());

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Creates a tapped Treasure for a Vehicle discard")
    void vehicleDiscardCreatesTappedTreasure() {
        lootAndDiscard(new RenegadeFreighter());

        assertThat(findPermanents(player1, "Treasure")).singleElement()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Does not create a Treasure for an unrelated discard")
    void unrelatedDiscardDoesNotCreateTreasure() {
        lootAndDiscard(new GrizzlyBears());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void lootAndDiscard(com.github.laxika.magicalvibes.model.Card discardedCard) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MaryReadAndAnneBonny());
        source.setSummoningSick(false);
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
