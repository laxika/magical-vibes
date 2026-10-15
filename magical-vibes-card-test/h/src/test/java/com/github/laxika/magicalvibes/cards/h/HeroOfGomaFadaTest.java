package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfGomaFada.class, GrizzlyBears.class, SeaGateLoremaster.class, Conspiracy.class})
class HeroOfGomaFadaTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures indestructible")
    void ownAllyEntryGrantsIndestructibleToYourCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new HeroOfGomaFada(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hero = findPermanent(player1, "Hero of Goma Fada");
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives all your creatures indestructible")
    void anotherAllyEntryGrantsIndestructibleToYourCreatures() {
        Permanent hero = addCreatureReady(player1, new HeroOfGomaFada());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new SeaGateLoremaster(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Sea Gate Loremaster");
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Hero of Goma Fada")
    void nonAllyEntryDoesNotTrigger() {
        Permanent hero = addCreatureReady(player1, new HeroOfGomaFada());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.castFromHand(player1, new HeroOfGomaFada(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hero = findPermanent(player1, "Hero of Goma Fada");
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger your Hero")
    void opponentAllyEntryDoesNotTrigger() {
        Permanent hero = addCreatureReady(player1, new HeroOfGomaFada());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SeaGateLoremaster(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Rally affects your creatures present at resolution but not opposing creatures")
    void grantsToCreaturesPresentAtResolution() {
        Permanent opposingCreature = addCreatureReady(player2, new HeroOfGomaFada());
        harness.castFromHand(player1, new HeroOfGomaFada(), "{4}{W}");
        harness.passBothPriorities();

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Rally resolves do not gain indestructible")
    void laterCreaturesDoNotGainIndestructible() {
        harness.castFromHand(player1, new HeroOfGomaFada(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hero of Goma Fada"),
                Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"),
                Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Hero's own entry triggers Rally even when it is not an Ally")
    void ownEntryTriggersWhenCreatureTypesAreReplaced() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.castFromHand(player1, new HeroOfGomaFada(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hero of Goma Fada"),
                Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
