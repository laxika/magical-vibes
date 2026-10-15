package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BeckCall;
import com.github.laxika.magicalvibes.cards.u.UncoveredClues;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CouncilOfTheAbsolute.class, UncoveredClues.class, BeckCall.class})
class CouncilOfTheAbsoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Council of the Absolute awaits a card name choice and records it")
    void resolvingChoosesCardName() {
        harness.setHand(player1, List.of(new CouncilOfTheAbsolute()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Uncovered Clues");

        Permanent council = findPermanent(player1, "Council of the Absolute");
        assertThat(council.getChosenName()).isEqualTo("Uncovered Clues");
    }

    @Test
    @DisplayName("Opponents can't cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyCouncil(player1, "Uncovered Clues");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new UncoveredClues()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponents can still cast spells with a different name")
    void opponentCanCastOtherNames() {
        addReadyCouncil(player1, "Beck");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new UncoveredClues()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller casts a spell with the chosen name for {2} less")
    void controllerGetsCostReduction() {
        addReadyCouncil(player1, "Uncovered Clues");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell with a different name gets no discount")
    void otherSpellsAreNotDiscounted() {
        addReadyCouncil(player1, "Beck");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotChooseCreatureName() {
        harness.setHand(player1, List.of(new CouncilOfTheAbsolute()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Council of the Absolute"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Council of the Absolute");
        harness.handleListChoice(player1, "Uncovered Clues");
        harness.assertOnBattlefield(player1, "Council of the Absolute");
    }

    @Test
    void discountDoesNotPayColoredMana() {
        addReadyCouncil(player1, "Uncovered Clues");
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleCouncilsDoNotRemoveColoredCost() {
        addReadyCouncil(player1, "Uncovered Clues");
        addReadyCouncil(player1, "Uncovered Clues");
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void prohibitionEndsWhenCouncilLeavesBattlefield() {
        Permanent council = addReadyCouncil(player1, "Uncovered Clues");
        gd.playerBattlefields.get(player1.getId()).remove(council);
        gd.playerGraveyards.get(player1.getId()).add(council.getCard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new UncoveredClues()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void namedSplitHalfCannotBeCastByOpponent() {
        addReadyCouncil(player1, "Beck");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BeckCall()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player2, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void namingEitherHalfProhibitsFusedSpellForOpponent() {
        addReadyCouncil(player1, "Beck");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BeckCall()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player2, 0, 2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentCanCastUnnamedSplitHalf() {
        addReadyCouncil(player1, "Beck");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        BeckCall physicalCard = new BeckCall();
        harness.setHand(player2, List.of(physicalCard));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalSorcery(player2, 0, 1, List.of());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).singleElement().isSameAs(physicalCard);
    }

    @Test
    void namingColoredOnlyHalfDiscountsFusedSpell() {
        addReadyCouncil(player1, "Beck");
        harness.setHand(player1, List.of(new BeckCall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalSorcery(player1, 0, 2, List.of());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void discountEndsWhenCouncilLeavesBattlefield() {
        Permanent council = addReadyCouncil(player1, "Uncovered Clues");
        gd.playerBattlefields.get(player1.getId()).remove(council);
        gd.playerGraveyards.get(player1.getId()).add(council.getCard());
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCouncil(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CouncilOfTheAbsolute());
        perm.setChosenName(chosenName);
        return perm;
    }
}
