package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hibernation.class, LlanowarElves.class, RagingGoblin.class, Forest.class, WildGrowth.class})
class HibernationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all green permanents to their owners' hands, regardless of controller")
    void returnsAllGreenPermanents() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Returns green noncreature permanents but not their colorless land host")
    void returnsGreenNoncreaturePermanents() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent wildGrowth = harness.addToBattlefieldAndReturn(player1, new WildGrowth());
        wildGrowth.setAttachedTo(forest.getId());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Wild Growth");
        harness.assertNotOnBattlefield(player1, "Wild Growth");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Does not return nongreen permanents")
    void doesNotReturnNongreenPermanents() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertNotInHand(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Resolves with no green permanents in play")
    void resolvesWithNoGreenPermanents() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Raging Goblin");
    }
}
