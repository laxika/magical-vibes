package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({AcademicDispute.class, EnvironmentalSciences.class, Forest.class, GrizzlyBears.class})
class AcademicDisputeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature must block this turn if able")
    void targetCreatureMustBlockIfAble() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        castAcademicDispute(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Accepting the reach choice grants reach until end of turn")
    void acceptingReachChoiceGrantsReach() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        castAcademicDispute(target, new Forest());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Learn searches the sideboard for a Lesson")
    void learnSearchesForLesson() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        Card lesson = new EnvironmentalSciences();
        Card nonLesson = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson, nonLesson)));

        castAcademicDispute(target);
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(lesson);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonLesson);
    }

    @Test
    @DisplayName("Learn can discard a card and draw a card")
    void learnDiscardsAndDraws() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        castAcademicDispute(target, discarded);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AcademicDispute()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A tapped creature is not required to block")
    void tappedCreatureCannotBeForcedToBlock() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        target.tap();

        castAcademicDispute(target);
        harness.handleMayAbilityChosen(player1, false);
        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
        assertThat(target.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Both reach and learning can be declined")
    void canDeclineReachAndLearn() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        Card retained = new Forest();
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castAcademicDispute(target, retained);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, -1);

        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An absent target prevents the entire spell from resolving, including learn")
    void absentTargetPreventsLearn() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));
        harness.setHand(player1, List.of(new AcademicDispute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        harness.assertInGraveyard(player1, "Academic Dispute");
    }

    @Test
    @DisplayName("The requirement to block expires at end of turn")
    void blockingRequirementExpires() {
        Permanent target = addReadyCreature(player2, new GrizzlyBears());
        castAcademicDispute(target);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    private void castAcademicDispute(Permanent target, Card... additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new AcademicDispute());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
