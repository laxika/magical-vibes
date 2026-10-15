package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RimrockKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StorytellerPixie.class, RimrockKnight.class, GrizzlyBears.class})
class StorytellerPixieTest extends BaseCardTest {

    @Test
    void drawsWhenAnAdventureSpellIsCast() {
        seedDeck();
        harness.addToBattlefield(player1, new StorytellerPixie());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenTheCreatureFaceOfAnAdventureCardIsCast() {
        seedDeck();
        harness.addToBattlefield(player1, new StorytellerPixie());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenANonAdventureSpellIsCast() {
        seedDeck();
        harness.addToBattlefield(player1, new StorytellerPixie());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void seedDeck() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new GrizzlyBears());
    }
}
