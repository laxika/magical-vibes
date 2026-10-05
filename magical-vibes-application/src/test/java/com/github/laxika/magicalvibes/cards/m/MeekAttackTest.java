package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeekAttack.class, GrizzlyBears.class, AirElemental.class, MeandersGuide.class})
class MeekAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only creature cards with total power and toughness 5 or less")
    void offersOnlyEligibleCreatures() {
        harness.addToBattlefield(player1, new MeekAttack());
        harness.setHand(player1, List.of(new GrizzlyBears(), new AirElemental()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
    }

    @Test
    @DisplayName("Puts the chosen creature onto the battlefield with haste and schedules its sacrifice")
    void putsChosenCreatureWithHasteAndEndStepSacrifice() {
        harness.addToBattlefield(player1, new MeekAttack());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves the eligible creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.addToBattlefield(player1, new MeekAttack());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Allows a total of exactly five and excludes noncreatures")
    void acceptsBoundaryTotalAndRejectsNoncreatures() {
        harness.addToBattlefield(player1, new MeekAttack());
        harness.setHand(player1, List.of(new MeandersGuide(), new MeekAttack(), new AirElemental()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Meanders Guide");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Accepting with no eligible card leaves the hand unchanged")
    void noEligibleCardDoesNothing() {
        harness.addToBattlefield(player1, new MeekAttack());
        harness.setHand(player1, List.of(new AirElemental(), new MeekAttack()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An activation during the end step waits for the next turn's end step")
    void endStepActivationWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new MeekAttack());
        harness.setHand(player1, List.of(new MeandersGuide()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Meanders Guide");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertOnBattlefield(player1, "Meanders Guide");
        assertThat(findPermanent(player1, "Meanders Guide").hasKeyword(Keyword.HASTE)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Meanders Guide");
        harness.assertInGraveyard(player1, "Meanders Guide");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
