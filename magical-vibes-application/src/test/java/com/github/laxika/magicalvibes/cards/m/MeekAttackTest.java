package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(bears.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
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

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
