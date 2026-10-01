package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArthurMarigoldKnight.class, FountainOfYouth.class, GrizzlyBears.class})
class ArthurMarigoldKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Needs Arthur and another attacking creature")
    void requiresAnotherAttacker() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new ArthurMarigoldKnight());
        Card topCreature = new GrizzlyBears();
        Card otherCard = new FountainOfYouth();
        harness.setLibrary(player1, List.of(topCreature, otherCard));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCreature, otherCard);
    }

    @Test
    @DisplayName("Puts a creature from the top six tapped and attacking, then returns it at end of combat")
    void putsCreatureTappedAndAttackingThenReturnsIt() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS,
                TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new ArthurMarigoldKnight());
        addCreatureReady(player1, new FountainOfYouth());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                new FountainOfYouth(),
                creature,
                new FountainOfYouth(),
                new FountainOfYouth(),
                new FountainOfYouth(),
                new FountainOfYouth()));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(entered);
    }
}
