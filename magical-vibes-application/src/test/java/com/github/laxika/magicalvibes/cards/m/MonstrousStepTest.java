package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MonstrousStep.class, GrizzlyBears.class})
class MonstrousStepTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the first target +7/+7 and makes the optional target block it")
    void boostsAndRequiresTheOptionalTargetToBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrousStep()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, List.of(attacker.getId(), blocker.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getPowerModifier()).isEqualTo(7);
        assertThat(attacker.getToughnessModifier()).isEqualTo(7);
        assertThat(findPermanent(player2, "Grizzly Bears").getMustBlockIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Can be cast without choosing the optional blocker")
    void optionalBlockerCanBeOmitted() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrousStep()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(7);
        assertThat(attacker.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("The optional target must be a different creature")
    void optionalTargetMustBeDifferent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonstrousStep()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MonstrousStep()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Monstrous Step");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The required block does not apply when the chosen creature cannot block")
    void cannotBlockDoesNotCreateAnIllegalBlockRequirement() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.tap();
        harness.setHand(player1, List.of(new MonstrousStep()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, List.of(attacker.getId(), blocker.getId()));
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
    }
}
