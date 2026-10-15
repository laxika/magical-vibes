package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.q.QuickStudy;
import com.github.laxika.magicalvibes.cards.t.TheFirstDoctor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingStagecoach.class, QuickStudy.class, Divination.class, LlanowarElves.class,
        Forest.class, GrizzlyBears.class, TheFirstDoctor.class})
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

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();
        assertCascadeHit("Llanowar Elves");
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();
        assertCascadeHit("Llanowar Elves");
    }

    @Test
    void powerUpdatesAsGraveyardChangesAndWorksOutsideBattlefield() {
        Permanent stagecoach = addStagecoachReady();
        assertThat(gqs.getEffectivePower(gd, stagecoach)).isZero();
        harness.setGraveyard(player1, List.of(new QuickStudy(), new Divination()));
        assertThat(gqs.getEffectivePower(gd, stagecoach)).isEqualTo(2);
        harness.setGraveyard(player1, List.of(new Divination()));
        assertThat(gqs.getEffectivePower(gd, stagecoach)).isOne();

        Card handStagecoach = new SmolderingStagecoach();
        harness.setHand(player1, List.of(handStagecoach));
        assertThat(gqs.getEffectiveCardPower(gd, handStagecoach)).isOne();
        Card graveyardStagecoach = new SmolderingStagecoach();
        harness.setGraveyard(player1, List.of(graveyardStagecoach, new QuickStudy(), new Divination()));
        assertThat(gqs.getEffectiveCardPower(gd, graveyardStagecoach)).isEqualTo(2);
    }

    @Test
    void onlyFirstInstantAfterAttackCascadesEvenAfterStagecoachLeaves() {
        Permanent stagecoach = attackWithStagecoach();
        gd.playerBattlefields.get(player1.getId()).remove(stagecoach);
        harness.setGraveyard(player1, List.of(stagecoach.getCard()));
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();
        assertCascadeHit("Llanowar Elves");
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        Card untouchedTop = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouchedTop, new Forest(), new Forest()));
        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(untouchedTop);
    }

    @Test
    void cascadeUsesSpellManaValueRatherThanStagecoachManaValue() {
        attackWithStagecoach();
        Card equalManaValue = new Divination();
        Card hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(new Forest(), equalManaValue, hit, new Forest(), new Forest()));

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();
        assertCascadeHit("Llanowar Elves");
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).contains(equalManaValue);
    }

    @Test
    void grantedCascadeTriggersTheFirstDoctor() {
        Permanent stagecoach = attackWithStagecoach();
        harness.addToBattlefield(player1, new TheFirstDoctor());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new QuickStudy(), "{2}{U}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, stagecoach.getId());
        resolveAllTriggers();
        assertThat(stagecoach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    private Permanent attackWithStagecoach() {
        Permanent stagecoach = addStagecoachReady();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        });
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, stagecoach)).isTrue();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        return stagecoach;
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
