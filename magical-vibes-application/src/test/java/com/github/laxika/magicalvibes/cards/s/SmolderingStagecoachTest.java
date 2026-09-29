package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.q.QuickStudy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingStagecoach.class, QuickStudy.class, Divination.class, LlanowarElves.class,
        Forest.class, GrizzlyBears.class})
class SmolderingStagecoachTest extends BaseCardTest {

    @Test
    void powerCountsOwnInstantAndSorceryCardsInGraveyard() {
        Permanent stagecoach = addStagecoachReady();
        harness.setGraveyard(player1, List.of(new QuickStudy(), new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new QuickStudy()));

        assertThat(gqs.getEffectivePower(gd, stagecoach)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stagecoach)).isEqualTo(5);
    }

    @Test
    void attackGrantsCascadeToTheNextInstantAndNextSorcerySeparately() {
        addStagecoachReady();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertCascadeHit("Llanowar Elves");
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertCascadeHit("Llanowar Elves");
    }

    private Permanent addStagecoachReady() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        return addCreatureReady(player1, new SmolderingStagecoach());
    }

    private void assertCascadeHit(String cardName) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly(cardName);
    }
}
