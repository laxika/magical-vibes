package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfNewBenalia.class})
class GuardianOfNewBenaliaTest extends BaseCardTest {

    @Test
    void enlistingTriggersScryTwo() {
        Permanent guardian = addGuardianReady();
        Permanent supporter = addCreatureReady(player1, new GuardianOfNewBenalia());
        harness.setLibrary(player1, List.of(new GuardianOfNewBenalia(), new GuardianOfNewBenalia()));

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(guardian.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void resolvingAbilityDiscardsCardTapsGuardianAndGrantsIndestructible() {
        Permanent guardian = addGuardianReady();
        harness.setHand(player1, List.of(new GuardianOfNewBenalia()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(guardian.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Guardian of New Benalia");
    }

    @Test
    void decliningEnlistDoesNotScryOrTapSupporter() {
        Permanent guardian = addGuardianReady();
        Permanent supporter = addGuardianReady();

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(supporter.isTapped()).isFalse();
        assertThat(guardian.getPowerModifier()).isZero();
    }

    @Test
    void discardIsPaidBeforeTappingAndIndestructibleResolve() {
        Permanent guardian = addGuardianReady();
        harness.setHand(player1, List.of(new GuardianOfNewBenalia()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Guardian of New Benalia");
        harness.assertNotInHand(player1, "Guardian of New Benalia");
        assertThat(guardian.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(guardian.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void alreadyTappedGuardianCanActivateRepeatedly() {
        Permanent guardian = addGuardianReady();
        guardian.tap();
        harness.setHand(player1, List.of(new GuardianOfNewBenalia(), new GuardianOfNewBenalia()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(guardian.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Guardian of New Benalia");
    }

    @Test
    void summoningSicknessDoesNotPreventDiscardAbility() {
        Permanent guardian = addGuardianReady();
        guardian.setSummoningSick(true);
        harness.setHand(player1, List.of(new GuardianOfNewBenalia()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(guardian.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent guardian = addGuardianReady();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(guardian.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void indestructibleExpiresAtEndOfTurn() {
        Permanent guardian = addGuardianReady();
        harness.setHand(player1, List.of(new GuardianOfNewBenalia()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void enlistingScriesAvailableCardWithOneCardLibrary() {
        Permanent guardian = addGuardianReady();
        Permanent supporter = addGuardianReady();
        GuardianOfNewBenalia libraryCard = new GuardianOfNewBenalia();
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(supporter.isTapped()).isTrue();
        assertThat(guardian.getPowerModifier()).isEqualTo(2);
    }

    private Permanent addGuardianReady() {
        return addCreatureReady(player1, new GuardianOfNewBenalia());
    }
}
