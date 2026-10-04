package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CollectiveUnconscious;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiveKidsInATrenchcoat.class, CollectiveUnconscious.class})
class FiveKidsInATrenchcoatTest extends BaseCardTest {

    @Test
    @DisplayName("Counts as five creatures for effects that count creatures")
    void countsAsFiveCreatures() {
        harness.addToBattlefield(player1, new FiveKidsInATrenchcoat());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new CollectiveUnconscious()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 5);
    }

    @Test
    void eachCopyContributesFiveCreatures() {
        harness.addToBattlefield(player1, new FiveKidsInATrenchcoat());
        harness.addToBattlefield(player1, new FiveKidsInATrenchcoat());
        harness.setHand(player1, List.of(new CollectiveUnconscious()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
    }

    @Test
    void opposingCopiesDoNotContributeToYourCreatureCount() {
        harness.addToBattlefield(player1, new FiveKidsInATrenchcoat());
        harness.addToBattlefield(player2, new FiveKidsInATrenchcoat());
        harness.setHand(player1, List.of(new CollectiveUnconscious()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    @CardUsed(Humble.class)
    void countsAsOneCreatureAfterLosingItsAbility() {
        var kids = harness.addToBattlefieldAndReturn(player1, new FiveKidsInATrenchcoat());
        harness.setHand(player1, List.of(new Humble(), new CollectiveUnconscious()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0, kids.getId());
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
