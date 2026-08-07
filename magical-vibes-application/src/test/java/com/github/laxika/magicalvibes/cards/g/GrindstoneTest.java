package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrindstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Repeats while each milled pair shares a color, stopping on the first mismatched pair")
    void repeatsWhilePairsShareAColor() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card survivor = new Shock();
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new Shock(), new Shock(), new GrizzlyBears(), new BottleGnomes(), survivor)));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(survivor);
    }

    @Test
    @DisplayName("A pair with no shared color mills only two cards")
    void mismatchedFirstPairStopsImmediately() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new Shock(), new GrizzlyBears(), new Shock(), new Shock())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Two colorless cards do not share a color, so the process stops")
    void colorlessPairDoesNotRepeat() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new BottleGnomes(), new BottleGnomes(), new Shock(), new Shock())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A one-card library mills that card and ends the process")
    void singleCardLibraryEndsProcess() {
        harness.addToBattlefield(player1, new Grindstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Shock())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
