package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkrelvDefectorMiteTest extends BaseCardTest {

    @Test
    @DisplayName("Skrelv deals a poison counter when it deals combat damage")
    void dealsToxicCombatDamage() {
        Permanent skrelv = addCreatureReady(player1, new SkrelvDefectorMite());
        skrelv.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Skrelv can't block")
    void cannotBlock() {
        Permanent skrelv = addCreatureReady(player1, new SkrelvDefectorMite());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(skrelv);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skrelv grants toxic and chosen-color evasion to another creature")
    void grantsToxicAndChosenColorEvasion() {
        Permanent skrelv = addCreatureReady(player1, new SkrelvDefectorMite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasKeyword(gd, target, Keyword.TOXIC)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, target, CardColor.RED)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        Permanent blocker = addCreatureReady(player2, new RagingGoblin());
        target.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skrelv's ability can resolve after Skrelv leaves the battlefield")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent skrelv = addCreatureReady(player1, new SkrelvDefectorMite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(skrelv);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasKeyword(gd, target, Keyword.TOXIC)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, target, CardColor.BLUE)).isTrue();
    }
}
