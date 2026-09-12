package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeBlizzardKitsune.class, GrizzlyBears.class})
class BladeBlizzardKitsuneTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Blade-Blizzard Kitsune in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeBlizzardKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent kitsune = findPermanent(player1, "Blade-Blizzard Kitsune");
        assertThat(kitsune.isTapped()).isTrue();
        assertThat(kitsune.isAttacking()).isTrue();
        assertThat(kitsune.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Blade-Blizzard Kitsune's double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsCombatDamageTwice() {
        Permanent kitsune = addCreatureReady(player1, new BladeBlizzardKitsune());
        kitsune.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ninjutsu can't return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BladeBlizzardKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }
}
