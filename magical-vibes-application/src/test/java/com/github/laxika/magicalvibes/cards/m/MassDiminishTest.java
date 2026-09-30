package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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

@CardUsed({MassDiminish.class, FountainOfYouth.class, GrizzlyBears.class, SerraAngel.class})
class MassDiminishTest extends BaseCardTest {

    @Test
    @DisplayName("Sets all creatures controlled by the target player to base 1/1")
    void setsTargetPlayersCreaturesToOneOne() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(player2.getId());

        assertThat(targetBear.getEffectivePower()).isEqualTo(1);
        assertThat(targetBear.getEffectiveToughness()).isEqualTo(1);
        assertThat(targetAngel.getEffectivePower()).isEqualTo(1);
        assertThat(targetAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The mass shrink lasts through cleanup and ends on the caster's next turn")
    void lastsUntilCastersNextTurn() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(player2.getId());
        assertThat(targetBear.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(targetBear.getEffectivePower()).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(targetBear.getEffectivePower()).isEqualTo(2);
        assertThat(targetBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback applies the same effect and exiles Mass Diminish")
    void flashbackAppliesEffectAndExilesSpell() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MassDiminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(targetBear.getEffectivePower()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Mass Diminish");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mass Diminish"));
    }

    @Test
    @DisplayName("Mass Diminish only accepts a player target")
    void rejectsPermanentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassDiminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    private void castAndResolve(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new MassDiminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
