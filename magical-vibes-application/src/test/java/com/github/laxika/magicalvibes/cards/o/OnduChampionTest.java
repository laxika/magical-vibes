package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnduChampion.class, ExpeditionEnvoy.class, GrizzlyBears.class})
class OnduChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives trample to your creatures")
    void ownAllyEntryGrantsTrampleToYourCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OnduChampion()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Ondu Champion");
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives trample to all your creatures")
    void anotherAllyEntryGrantsTrampleToYourCreatures() {
        Permanent champion = addCreatureReady(player1, new OnduChampion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExpeditionEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Expedition Envoy");
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Ondu Champion")
    void nonAllyEntryDoesNotTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new OnduChampion());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new OnduChampion()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Ondu Champion");
        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, champion, Keyword.TRAMPLE)).isFalse();
    }
}
