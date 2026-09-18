package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MischievousQuanar;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RushOfKnowledge.class, MischievousQuanar.class, RavenGuildInitiate.class,
        ScornfulEgotist.class})
class RushOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the greatest mana value among your permanents")
    void drawsForGreatestControlledPermanentManaValue() {
        harness.addToBattlefield(player1, new MischievousQuanar());
        harness.addToBattlefield(player1, new RavenGuildInitiate());
        harness.addToBattlefield(player2, new ScornfulEgotist());
        harness.setLibrary(player1, List.of(
                new RavenGuildInitiate(), new RavenGuildInitiate(), new RavenGuildInitiate(),
                new RavenGuildInitiate(), new RavenGuildInitiate(), new RavenGuildInitiate(),
                new RavenGuildInitiate(), new RavenGuildInitiate()));
        harness.castFromHand(player1, new RushOfKnowledge(), "{4}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Evaluates the greatest mana value when the spell resolves")
    void evaluatesGreatestManaValueAtResolution() {
        harness.addToBattlefield(player1, new MischievousQuanar());
        harness.setLibrary(player1, List.of(
                new RavenGuildInitiate(), new RavenGuildInitiate(), new RavenGuildInitiate(),
                new RavenGuildInitiate(), new RavenGuildInitiate()));
        harness.castFromHand(player1, new RushOfKnowledge(), "{4}{U}");

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new RavenGuildInitiate());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Draws no cards when you control no permanents")
    void drawsNothingWithoutControlledPermanents() {
        harness.castFromHand(player1, new RushOfKnowledge(), "{4}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rush of Knowledge");
    }

    @Test
    @DisplayName("Treats a face-down permanent as having mana value zero")
    void treatsFaceDownPermanentAsManaValueZero() {
        harness.setHand(player1, List.of(new MischievousQuanar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.isFaceDown())).isTrue();

        harness.setLibrary(player1, List.of(
                new RavenGuildInitiate(), new RavenGuildInitiate(), new RavenGuildInitiate(),
                new RavenGuildInitiate(), new RavenGuildInitiate()));
        harness.castFromHand(player1, new RushOfKnowledge(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
