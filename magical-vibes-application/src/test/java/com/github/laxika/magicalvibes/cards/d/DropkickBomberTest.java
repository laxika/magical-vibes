package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DropkickBomber.class, RagingGoblin.class, GrizzlyBears.class})
class DropkickBomberTest extends BaseCardTest {

    @Test
    @DisplayName("Other Goblins you control get +1/+1")
    void buffsOtherGoblinsYouControl() {
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent bomber = addCreatureReady(player1, new DropkickBomber());
        Permanent nonGoblin = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGoblin = addCreatureReady(player2, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bomber)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bomber)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability grants another Goblin flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new DropkickBomber());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Targeted Goblin is sacrificed after dealing combat damage")
    void sacrificesTargetAfterCombatDamage() {
        addCreatureReady(player1, new DropkickBomber());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        goblin.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Ability can target only another Goblin you control")
    void restrictsTargets() {
        Permanent bomber = addCreatureReady(player1, new DropkickBomber());
        Permanent ownGoblin = addCreatureReady(player1, new RagingGoblin());
        Permanent ownNonGoblin = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGoblin = addCreatureReady(player2, new RagingGoblin());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bomber.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownNonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, ownGoblin.getId());
    }
}
