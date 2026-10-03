package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorJaneFoster.class, GrizzlyBears.class, HillGiant.class, Plains.class, TrainedArmodon.class})
class DoctorJaneFosterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card with mana value 3 or less to hand")
    void returnsCreatureToHandWithoutLifeGain() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castDoctorJaneFoster();
        chooseTarget(target);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Doctor Jane Foster");
    }

    @Test
    @DisplayName("Returns the target creature card to the battlefield if you gained life this turn")
    void returnsCreatureToBattlefieldWithLifeGain() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castDoctorJaneFoster();
        chooseTarget(target);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only targets creature cards with mana value 3 or less")
    void filtersGraveyardTargets() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(tooExpensive, eligible));

        castDoctorJaneFoster();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("Targets a mana value three creature but not a land or an opponent's creature")
    void includesManaValueThreeAndExcludesOtherGraveyardCards() {
        Card eligible = new TrainedArmodon();
        Card land = new Plains();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, land));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castDoctorJaneFoster();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        chooseTarget(eligible);

        harness.assertInHand(player1, "Trained Armodon");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Life gained after choosing the target changes the return destination")
    void checksLifeGainAtResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        castDoctorJaneFoster();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent gaining life does not change the return destination")
    void opponentLifeGainDoesNotReanimate() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1));

        castDoctorJaneFoster();
        chooseTarget(target);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return another card when the chosen target leaves the graveyard")
    void missingTargetDoesNotReturnAnotherCreature() {
        Card target = new GrizzlyBears();
        Card other = new TrainedArmodon();
        harness.setGraveyard(player1, List.of(target, other));
        castDoctorJaneFoster();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Trained Armodon");
        harness.assertNotInHand(player1, "Trained Armodon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters normally when there are no legal graveyard targets")
    void entersWithoutLegalTargets() {
        harness.setGraveyard(player1, List.of(new HillGiant()));

        castDoctorJaneFoster();

        harness.assertOnBattlefield(player1, "Doctor Jane Foster");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castDoctorJaneFoster() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new DoctorJaneFoster(), "{3}{W}");
        harness.passBothPriorities();
    }

    private void chooseTarget(Card target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
