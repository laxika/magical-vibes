package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatientInstructor.class, Forest.class})
class PatientInstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Recruit creates a Soldier after discarding a nonland card")
    void recruitCreatesSoldierForNonlandDiscard() {
        castAndResolve(new PatientInstructor(), new Forest());

        harness.assertInGraveyard(player1, "Patient Instructor");
        assertThat(tokens()).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Recruit does not create a Soldier after discarding a land card")
    void recruitDoesNotCreateSoldierForLandDiscard() {
        castAndResolve(new Forest(), new PatientInstructor());

        harness.assertInGraveyard(player1, "Forest");
        assertThat(tokens()).isEmpty();
        harness.assertInHand(player1, "Patient Instructor");
    }

    @Test
    @DisplayName("The recruited token is both a Human and a Soldier")
    void recruitedTokenHasBothCreatureTypes() {
        castAndResolve(new PatientInstructor(), new Forest());

        assertThat(tokens()).hasSize(1);
        assertThat(tokens().getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Recruit creates its token before returning priority after the discard")
    void tokenCreationIsPartOfRecruitResolution() {
        beginRecruit(new PatientInstructor(), new Forest());

        harness.handleCardChosen(player1, 0);

        assertThat(tokens()).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Recruit can discard the card it just drew")
    void canDiscardDrawnNonlandCard() {
        beginRecruit(new Forest(), new PatientInstructor());

        harness.assertInHand(player1, "Patient Instructor");
        harness.handleCardChosen(player1, 1);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Patient Instructor");
        harness.assertInHand(player1, "Forest");
        assertThat(tokens()).hasSize(1);
    }

    private List<Permanent> tokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void castAndResolve(Card discardedCard, Card drawnCard) {
        beginRecruit(discardedCard, drawnCard);
        harness.handleCardChosen(player1, 0);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void beginRecruit(Card otherCard, Card drawnCard) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new PatientInstructor(), otherCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
