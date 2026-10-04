package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DireFleetHoarder;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellFlagship.class, AirElemental.class, DireFleetHoarder.class, QueensBaySoldier.class})
class FellFlagshipTest extends BaseCardTest {


    @Test
    @DisplayName("Boosts Pirates you control with +1/+0")
    void boostsPirates() {
        addFellFlagshipReady(player1);
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new DireFleetHoarder());

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(3);     // 2 base + 1 lord
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(1); // 1 base + 0 lord
    }

    @Test
    @DisplayName("Does not boost non-Pirate creatures")
    void doesNotBoostNonPirates() {
        addFellFlagshipReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's Pirates")
    void doesNotBoostOpponentPirates() {
        addFellFlagshipReady(player1);
        Permanent opponentPirate = harness.addToBattlefieldAndReturn(player2, new DireFleetHoarder());

        assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentPirate)).isEqualTo(1);
    }


    @Test
    @DisplayName("Fell Flagship is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent flagship = addFellFlagshipReady(player1);

        assertThat(gqs.isCreature(gd, flagship)).isFalse();
    }

    @Test
    @DisplayName("Crewing with a single creature of sufficient power animates Fell Flagship")
    void crewWithSingleCreature() {
        Permanent flagship = addFellFlagshipReady(player1);
        Permanent crew = addCreatureReady(player1, new AirElemental()); // 4/4, power >= 3

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flagship.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addFellFlagshipReady(player1);
        addCreatureReady(player1, new QueensBaySoldier()); // power 2 < 3

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("The Pirate boost lets a summoning-sick Pirate pay crew 3")
    void boostedSummoningSickPirateCanCrew() {
        Permanent flagship = addFellFlagshipReady(player1);
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new DireFleetHoarder());
        pirate.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(pirate.isTapped()).isTrue();
        assertThat(flagship.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(gqs.getEffectivePower(gd, flagship)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, flagship)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple creatures can combine their power to pay crew 3")
    void multipleCreaturesCanCrew() {
        Permanent flagship = addFellFlagshipReady(player1);
        Permanent first = addCreatureReady(player1, new QueensBaySoldier());
        Permanent second = addCreatureReady(player1, new QueensBaySoldier());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, flagship)).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay crew")
    void tappedAndOpposingCreaturesCannotCrew() {
        addFellFlagshipReady(player1);
        Permanent tapped = addCreatureReady(player1, new AirElemental());
        tapped.tap();
        addCreatureReady(player2, new AirElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent flagship = addFellFlagshipReady(player1);
        addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, flagship)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flagship.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, flagship)).isFalse();
    }


    @Test
    @DisplayName("Damaged player must discard a card when Fell Flagship deals combat damage")
    void discardOnCombatDamage() {
        Permanent flagship = addFellFlagshipReady(player1);
        flagship.setAnimatedUntilEndOfTurn(true);
        flagship.setAnimatedPower(3);
        flagship.setAnimatedToughness(3);
        flagship.setAttacking(true);

        harness.setHand(player2, new ArrayList<>(List.of(new QueensBaySoldier())));

        resolveCombat();

        // The combat damage trigger is on the stack — resolve it
        harness.passBothPriorities();

        // Game pauses for discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
    }

    @Test
    @DisplayName("No discard trigger when Fell Flagship is blocked")
    void noDiscardWhenBlocked() {
        Permanent flagship = addFellFlagshipReady(player1);
        flagship.setAnimatedUntilEndOfTurn(true);
        flagship.setAnimatedPower(3);
        flagship.setAnimatedToughness(3);
        flagship.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setHand(player2, new ArrayList<>(List.of(new QueensBaySoldier())));

        resolveCombat();
        assertThat(gd.stack).isEmpty();

        // No discard prompt — Fell Flagship didn't deal combat damage to a player
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No input needed when opponent has no cards to discard")
    void noInputWhenOpponentHandEmpty() {
        Permanent flagship = addFellFlagshipReady(player1);
        flagship.setAnimatedUntilEndOfTurn(true);
        flagship.setAnimatedPower(3);
        flagship.setAnimatedToughness(3);
        flagship.setAttacking(true);

        harness.setHand(player2, new ArrayList<>());

        resolveCombat();
        resolveAllTriggers();

        // Discard does nothing, no input needed
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }


    @Test
    @DisplayName("The damaged player chooses exactly one discard even after the Vehicle leaves")
    void damagedPlayerChoosesDiscardAfterSourceLeaves() {
        Permanent flagship = addFellFlagshipReady(player2);
        addCreatureReady(player2, new AirElemental());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        flagship.setAttacking(true);
        harness.setHand(player1, List.of(new QueensBaySoldier(), new AirElemental()));
        harness.setHand(player2, List.of(new QueensBaySoldier()));

        resolveCombat(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(flagship);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addFellFlagshipReady(Player player) {
        return addCreatureReady(player, new FellFlagship());
    }
}
