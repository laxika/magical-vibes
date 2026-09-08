package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FallajiVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry triggers and gives a target creature +2/+0")
    void ownEntryTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallajiVanguard()));
        addFallajiVanguardMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent boostedTarget = findPermanent(player2, target.getId());
        assertThat(boostedTarget.getPowerModifier()).isEqualTo(2);
        assertThat(boostedTarget.getToughnessModifier()).isZero();
        assertThat(boostedTarget.getEffectivePower()).isEqualTo(4);
        assertThat(boostedTarget.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Another creature entering under its controller's control triggers the ability")
    void anotherAllyEntryTriggers() {
        harness.addToBattlefield(player1, new FallajiVanguard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The temporary power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallajiVanguard()));
        addFallajiVanguardMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent boostedTarget = findPermanent(player2, target.getId());
        assertThat(boostedTarget.getPowerModifier()).isEqualTo(2);
        assertThat(boostedTarget.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        boostedTarget = findPermanent(player2, target.getId());
        assertThat(boostedTarget.getPowerModifier()).isZero();
        assertThat(boostedTarget.getToughnessModifier()).isZero();
        assertThat(boostedTarget.getEffectivePower()).isEqualTo(2);
        assertThat(boostedTarget.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent findPermanent(Player player, UUID id) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private void addFallajiVanguardMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
