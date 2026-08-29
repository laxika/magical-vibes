package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetractionHelixTest extends BaseCardTest {

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent grantAbilityToReadyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);

        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }

    @Test
    @DisplayName("Target creature gains the ability to return a nonland permanent to its owner's hand")
    void grantedAbilityBouncesNonlandPermanent() {
        Permanent creature = grantAbilityToReadyCreature();
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bounceTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bounceTarget);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability cannot target a land")
    void grantedAbilityCannotTargetLand() {
        grantAbilityToReadyCreature();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("The granted ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        Permanent creature = grantAbilityToReadyCreature();
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        endTurn();
        creature.setSummoningSick(false);

        UUID bounceTarget = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bounceTarget))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Retraction Helix cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
