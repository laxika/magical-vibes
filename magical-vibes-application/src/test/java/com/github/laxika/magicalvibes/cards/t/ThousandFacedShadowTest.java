package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ThousandFacedShadow.class, GrizzlyBears.class})
class ThousandFacedShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu creates a tapped and attacking copy of another attacker")
    void ninjutsuCopiesAnotherAttackingCreature() {
        Permanent returnedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
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

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        Permanent shadow = findPermanent(player1, "Thousand-Faced Shadow");
        assertThat(shadow.isTapped()).isTrue();
        Permanent token = bears.stream().filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isTrue();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The enter trigger does not copy when the creature is not attacking")
    void hardCastDoesNotCopyWhileNotAttacking() {
        harness.setHand(player1, List.of(new ThousandFacedShadow()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thousand-Faced Shadow")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger only allows another attacking creature as its target")
    void enterTriggerRejectsNonAttackingTarget() {
        Permanent returnedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
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
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();
    }
}
