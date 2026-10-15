package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProphetOfTheScarab.class, DiregrafGhoul.class, Forest.class, GrizzlyBears.class})
class ProphetOfTheScarabTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws based on the greater of controlled Zombies and graveyard Zombie cards")
    void drawsUsingControlledZombieCountWhenItIsGreater() {
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.addToBattlefield(player1, new DiregrafGhoul());
        harness.setGraveyard(player1, List.of(new DiregrafGhoul()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ProphetOfTheScarab()));
        addProphetMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB draws based on graveyard Zombie cards when that count is greater")
    void drawsUsingGraveyardZombieCountWhenItIsGreater() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new DiregrafGhoul(), new DiregrafGhoul(), new DiregrafGhoul()));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ProphetOfTheScarab()));
        addProphetMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void addProphetMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
