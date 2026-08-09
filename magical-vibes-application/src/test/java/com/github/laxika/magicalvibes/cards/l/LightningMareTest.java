package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LightningMareTest extends BaseCardTest {

    @Test
    @DisplayName("Lightning Mare cannot be countered")
    void cannotBeCountered() {
        LightningMare mare = new LightningMare();
        harness.setHand(player1, List.of(mare));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mare.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Mare");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Lightning Mare can't be blocked by a blue creature")
    void cannotBeBlockedByBlueCreature() {
        Permanent blocker = new Permanent(new FugitiveWizard());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent mare = new Permanent(new LightningMare());
        mare.setSummoningSick(false);
        mare.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mare);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mare);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lightning Mare can be blocked by a non-blue creature")
    void canBeBlockedByNonBlueCreature() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent mare = new Permanent(new LightningMare());
        mare.setSummoningSick(false);
        mare.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mare);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mare);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("{1}{R}: Lightning Mare gets +1/+0 until end of turn")
    void pumpAbilityBoostsPowerUntilEndOfTurn() {
        Permanent mare = new Permanent(new LightningMare());
        mare.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mare);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mare.getPowerModifier()).isEqualTo(1);
        assertThat(mare.getToughnessModifier()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mare.getPowerModifier()).isEqualTo(0);
    }
}
