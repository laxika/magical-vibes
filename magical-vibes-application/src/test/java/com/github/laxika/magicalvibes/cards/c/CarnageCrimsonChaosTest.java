package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarnageCrimsonChaos.class, GrizzlyBears.class, HillGiant.class, HolyDay.class})
class CarnageCrimsonChaosTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets a creature card with mana value 3 or less")
    void etbTargetsEligibleCreature() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonCreature));

        castCarnageFromHand();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The returned creature must attack each combat if able")
    void returnedCreatureMustAttack() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The returned creature is sacrificed after dealing combat damage to a player")
    void returnedCreatureIsSacrificedAfterCombatDamage() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        castCarnageFromHand();
        chooseReturnedCreature(eligible);

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        returned.setSummoningSick(false);
        returned.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mayhem casts Carnage from the graveyard after it was discarded this turn")
    void mayhemCastsFromGraveyard() {
        GrizzlyBears eligible = new GrizzlyBears();
        CarnageCrimsonChaos carnage = new CarnageCrimsonChaos();
        harness.setGraveyard(player1, List.of(eligible, carnage));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(carnage.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromGraveyard(player1, 1);
        harness.passBothPriorities();
        chooseReturnedCreature(eligible);

        harness.assertOnBattlefield(player1, "Carnage, Crimson Chaos");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void castCarnageFromHand() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new CarnageCrimsonChaos()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseReturnedCreature(Card card) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
