package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightScavengers.class, LlanowarElves.class, GrizzlyBears.class,
        HillGiant.class, LeoninScimitar.class, PhyrexianRager.class, GrafRats.class})
class MidnightScavengersTest extends BaseCardTest {

    private void castMidnightScavengers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MidnightScavengers(), "{4}{B}");
        harness.passBothPriorities(); // resolve creature → ETB graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a MV≤3 creature card from graveyard to hand")
    void etbReturnsLowManaCreatureToHand() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(elves.getId());

        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Low mana value creatures are legal targets; MV 4+ and non-creatures are not")
    void onlyCreaturesWithManaValueAtMost3AreValid() {
        GrizzlyBears bears = new GrizzlyBears(); // MV 2
        HillGiant giant = new HillGiant(); // MV 4
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(bears, giant, scimitar));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Optional return can be declined")
    void returnCanBeDeclined() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        castMidnightScavengers();

        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Empty graveyard produces no graveyard choice")
    void emptyGraveyardNoChoice() {
        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Midnight Scavengers");
    }

    @Test
    void eligibleCreatureMustBeTargetedEvenWhenReturnWillBeDeclined() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        castMidnightScavengers();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player2, List.of(elves));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionIsNotReturned() {
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));
        castMidnightScavengers();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({PhyrexianRager.class})
    void manaValueThreeCreatureIsAnEligibleTarget() {
        PhyrexianRager rager = new PhyrexianRager();
        harness.setGraveyard(player1, List.of(rager));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(rager.getId());
    }

    @Test
    void graveyardContainingOnlyIneligibleCreaturesProducesNoChoice() {
        harness.setGraveyard(player1, List.of(new MidnightScavengers()));

        castMidnightScavengers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Midnight Scavengers");
        harness.assertNotInHand(player1, "Midnight Scavengers");
        harness.assertOnBattlefield(player1, "Midnight Scavengers");
    }

    @Test
    void returnRemainsOptionalAfterScavengersLeaveBattlefield() {
        GrafRats rats = new GrafRats();
        harness.setGraveyard(player1, List.of(rats));
        castMidnightScavengers();
        harness.handleMultipleCardsChosen(player1, List.of(rats.getId()));
        var scavengers = findPermanent(player1, "Midnight Scavengers");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, scavengers));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Graf Rats");
        harness.assertNotInHand(player1, "Graf Rats");
    }
}
