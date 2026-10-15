package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.m.MarchOfSwirlingMist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThousandFacedShadow.class, BearerOfMemory.class, TezzeretBetrayerOfFlesh.class,
        MarchOfSwirlingMist.class})
class ThousandFacedShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu creates a tapped and attacking copy of another attacker")
    void ninjutsuCopiesAnotherAttackingCreature() {
        Permanent returnedAttacker = addCreatureReady(player1, new BearerOfMemory());
        Permanent otherAttacker = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, returnedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player1, "Bearer of Memory");
        assertThat(bears).hasSize(2);
        Permanent shadow = findPermanent(player1, "Thousand-Faced Shadow");
        assertThat(shadow.isTapped()).isTrue();
        Permanent token = bears.stream().filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isTrue();
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("The enter trigger does not copy when the creature is not attacking")
    void hardCastDoesNotCopyWhileNotAttacking() {
        harness.castFromHand(player1, new ThousandFacedShadow(), "{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thousand-Faced Shadow")).hasSize(1);
        assertThat(findPermanents(player1, "Bearer of Memory")).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger only allows another attacking creature as its target")
    void enterTriggerRejectsNonAttackingTarget() {
        Permanent returnedAttacker = addCreatureReady(player1, new BearerOfMemory());
        Permanent otherAttacker = addCreatureReady(player1, new BearerOfMemory());
        Permanent bystander = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, returnedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
        Permanent shadow = findPermanent(player1, "Thousand-Faced Shadow");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, shadow.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The token can attack a planeswalker while the original creatures attack its controller")
    void tokenCanChooseADifferentDefender() {
        Permanent returnedAttacker = addCreatureReady(player1, new BearerOfMemory());
        Permanent otherAttacker = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());
        Permanent planeswalker = addCreatureReady(player2, new TezzeretBetrayerOfFlesh());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, returnedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handlePermanentChosen(player1, planeswalker.getId()));

        Permanent token = findPermanents(player1, "Bearer of Memory").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(planeswalker.getId());
        assertThat(otherAttacker.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Entering as the only attacker does not allow the Shadow to copy itself")
    void noCopyWhenThereIsNoOtherAttacker() {
        Permanent returnedAttacker = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, returnedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Thousand-Faced Shadow")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertInHand(player1, "Bearer of Memory");
    }

    @Test
    @DisplayName("A token copy of an attacking Shadow does not trigger its own copy ability")
    void copiedShadowDoesNotEnterFromHand() {
        Permanent returnedAttacker = addCreatureReady(player1, new BearerOfMemory());
        Permanent otherAttacker = addCreatureReady(player1, new ThousandFacedShadow());
        addCreatureReady(player2, new BearerOfMemory());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, returnedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thousand-Faced Shadow")).hasSize(3);
        assertThat(findPermanents(player1, "Thousand-Faced Shadow"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The copy ability creates no token if its target phases out in response")
    void phasedOutTargetCannotBeCopied() {
        Permanent returnedAttacker = addCreatureReady(player1, new BearerOfMemory());
        Permanent otherAttacker = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());
        declareAttackers(List.of(0, 1));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, returnedAttacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherAttacker.getId());

        harness.setHand(player2, List.of(new MarchOfSwirlingMist()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castInstantForX(player2, 0, 1, List.of(otherAttacker.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(otherAttacker);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
