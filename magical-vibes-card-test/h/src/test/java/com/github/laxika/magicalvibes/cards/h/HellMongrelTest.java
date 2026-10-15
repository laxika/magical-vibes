package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellMongrel.class, GrizzlyBears.class})
class HellMongrelTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card gives this creature +1/+1 until end of turn")
    void discardBoostsPlusOneOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        int basePower = gqs.getEffectivePower(gd, mongrel);
        int baseToughness = gqs.getEffectiveToughness(gd, mongrel);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        int basePower = gqs.getEffectivePower(gd, mongrel);
        int baseToughness = gqs.getEffectiveToughness(gd, mongrel);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations discard immediately and accumulate only on resolution")
    void repeatedActivationsAccumulate() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mongrel = harness.addToBattlefieldAndReturn(player1, new HellMongrel());
        int basePower = gqs.getEffectivePower(gd, mongrel);
        int baseToughness = gqs.getEffectiveToughness(gd, mongrel);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness + 1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Discarded Hell Mongrel can be cast for three mana on the opponent's turn")
    void madnessCastsForTwoGenericAndOneBlack() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        HellMongrel discarded = discardMongrelForMadness();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(discarded.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(discarded.getId()));
    }

    @Test
    @DisplayName("Declining madness puts the discarded Hell Mongrel into its owner's graveyard")
    void decliningMadnessMovesToGraveyard() {
        HellMongrel discarded = discardMongrelForMadness();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(discarded.getId()));
    }

    private HellMongrel discardMongrelForMadness() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new HellMongrel());
        HellMongrel discarded = new HellMongrel();
        harness.setHand(player1, List.of(discarded));
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
        for (int resolution = 0; resolution < 2
                && !(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice); resolution++) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return discarded;
    }
}
