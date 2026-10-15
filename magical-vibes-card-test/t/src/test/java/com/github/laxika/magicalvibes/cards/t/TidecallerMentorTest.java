package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidecallerMentor.class, GrizzlyBears.class, Forest.class})
class TidecallerMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB returns a target nonland permanent to its owner's hand")
    void thresholdReturnsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardCards(7));

        castTidecallerMentor();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Threshold ETB does not trigger below seven graveyard cards")
    void thresholdDoesNotTriggerBelowSevenCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardCards(6));

        castTidecallerMentor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Threshold ETB can resolve without choosing a target")
    void thresholdCanResolveWithoutTarget() {
        harness.setGraveyard(player1, graveyardCards(7));

        castTidecallerMentor();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tidecaller Mentor");
    }

    @Test
    @DisplayName("Threshold ETB only offers nonland permanents as targets")
    void thresholdTargetChoiceExcludesLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setGraveyard(player1, graveyardCards(7));

        castTidecallerMentor();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId());
    }

    @Test
    @DisplayName("Threshold is checked again when the triggered ability resolves")
    void thresholdMustStillBeMetAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardCards(7));

        castTidecallerMentor();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, graveyardCards(6));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's graveyard does not satisfy threshold")
    void thresholdCountsOnlyControllersGraveyard() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.setGraveyard(player2, graveyardCards(7));

        castTidecallerMentor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tidecaller Mentor");
    }

    @Test
    @DisplayName("Tidecaller Mentor can target itself after entering")
    void thresholdCanReturnItself() {
        harness.setGraveyard(player1, graveyardCards(7));

        castTidecallerMentor();
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Tidecaller Mentor"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tidecaller Mentor");
        harness.assertInHand(player1, "Tidecaller Mentor");
    }

    @Test
    @DisplayName("Threshold counts land cards in the graveyard")
    void thresholdCountsAllCardTypes() {
        List<Card> lands = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            lands.add(new Forest());
        }
        harness.setGraveyard(player1, lands);

        castTidecallerMentor();
        harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Tidecaller Mentor"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tidecaller Mentor");
        harness.assertInHand(player1, "Tidecaller Mentor");
    }

    private void castTidecallerMentor() {
        harness.castFromHand(player1, new TidecallerMentor(), "{1}{U}{B}");
        resolveAllTriggers();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
