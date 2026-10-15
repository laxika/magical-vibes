package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Foresee;
import com.github.laxika.magicalvibes.cards.l.LumithreadField;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScoutsWarning.class, BladeOfTheSixthPride.class, Foresee.class, SproutSwarm.class,
        DryadArbor.class, LumithreadField.class})
class ScoutsWarningTest extends BaseCardTest {

    private Card resolveScoutsWarning() {
        Card drawnCard = new BladeOfTheSixthPride();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new ScoutsWarning()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        return drawnCard;
    }

    @Test
    @DisplayName("Draws a card on resolution")
    void drawsACard() {
        Card drawnCard = resolveScoutsWarning();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The next creature spell can be cast at instant speed")
    void grantsFlashToNextCreature() {
        resolveScoutsWarning();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blade of the Sixth Pride");
    }

    @Test
    @DisplayName("Only the first creature spell gains flash")
    void grantIsConsumedByTheFirstCreature() {
        resolveScoutsWarning();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeOfTheSixthPride(), new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature spells are not granted flash")
    void doesNotGrantFlashToOtherTypes() {
        resolveScoutsWarning();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Foresee()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Casting a noncreature spell does not consume the creature permission")
    void noncreatureSpellDoesNotConsumeGrant() {
        resolveScoutsWarning();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blade of the Sixth Pride");
    }

    @Test
    @DisplayName("The flash permission applies only to the warning's controller")
    void grantDoesNotApplyToOpponent() {
        resolveScoutsWarning();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BladeOfTheSixthPride()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flash permission wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        resolveScoutsWarning();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A creature cast at normal sorcery timing consumes the permission")
    void normalTimingCreatureConsumesGrant() {
        resolveScoutsWarning();
        harness.setHand(player1, List.of(new BladeOfTheSixthPride(), new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple Warnings all apply to the same next creature")
    void multipleWarningsAreConsumedTogether() {
        resolveScoutsWarning();
        resolveScoutsWarning();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeOfTheSixthPride(), new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A noncreature morph card can be cast face down and consumes the permission")
    void faceDownCreatureConsumesGrant() {
        resolveScoutsWarning();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LumithreadField(), new BladeOfTheSixthPride()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isFaceDown()).isTrue();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Dryad Arbor can be played during combat on the controller's turn")
    void permitsCreatureLandOutsideMainPhaseOnOwnTurn() {
        resolveScoutsWarning();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DryadArbor()));

        harness.playLand(player1, 0);

        assertThat(countPermanents(player1, "Dryad Arbor")).isEqualTo(1);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dryad Arbor still cannot be played on an opponent's turn")
    void doesNotPermitCreatureLandOnOpponentsTurn() {
        resolveScoutsWarning();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DryadArbor()));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(countPermanents(player1, "Dryad Arbor")).isZero();
    }
}
