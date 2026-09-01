package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkaBoldBoomeranger.class, GrizzlyBears.class, Forest.class,
        Spellbook.class, FirebendingLesson.class})
class SokkaBoldBoomerangerTest extends BaseCardTest {

    @Test
    void entersAndDiscardsUpToTwoCardsThenDrawsThatMany() {
        Card discardedFirst = new GrizzlyBears();
        Card discardedSecond = new GrizzlyBears();
        Card drawnFirst = new Forest();
        Card drawnSecond = new Forest();
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond));
        harness.setHand(player1, List.of(new SokkaBoldBoomeranger(), discardedFirst, discardedSecond));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(2);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedFirst, discardedSecond);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
    }

    @Test
    void canDiscardFewerThanTwoCardsAndDrawsTheChosenAmount() {
        Card discarded = new GrizzlyBears();
        Card kept = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new SokkaBoldBoomeranger(), discarded, kept));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
    }

    @Test
    void artifactAndLessonSpellsPutCountersOnSokka() {
        harness.addToBattlefield(player1, new SokkaBoldBoomeranger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        Permanent sokka = findPermanent(player1, "Sokka, Bold Boomeranger");
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(sokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(sokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void unrelatedSpellsDoNotPutCountersOnSokka() {
        harness.addToBattlefield(player1, new SokkaBoldBoomeranger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent sokka = findPermanent(player1, "Sokka, Bold Boomeranger");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(sokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
