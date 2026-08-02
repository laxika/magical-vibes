package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevouringLightTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target attacking creature")
    void exilesAttackingCreature() {
        Permanent attacker = addAttacker(player1);

        castDevouringLight(TurnStep.DECLARE_ATTACKERS, attacker.getId());
        harness.passBothPriorities();

        assertExiled(attacker);
    }

    @Test
    @DisplayName("Exiles the target blocking creature")
    void exilesBlockingCreature() {
        Permanent blocker = addBlocker(player1);

        castDevouringLight(TurnStep.DECLARE_BLOCKERS, blocker.getId());
        harness.passBothPriorities();

        assertExiled(blocker);
    }

    @Test
    @DisplayName("Convoke taps a creature to help cast Devouring Light")
    void convokeTapsCreature() {
        Permanent target = addAttacker(player2);
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DevouringLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertExiled(target);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = addAttacker(player2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DevouringLight()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
        assertThat(attacker.isAttacking()).isTrue();
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = harness.addToBattlefieldAndReturn(owner, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }

    private void castDevouringLight(TurnStep step, UUID targetId) {
        harness.forceStep(step);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DevouringLight()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
    }

    private void assertExiled(Permanent target) {
        GameData gameData = harness.getGameData();
        assertThat(gameData.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gameData.playerBattlefields.values())
                .allSatisfy(battlefield -> assertThat(battlefield).noneMatch(permanent -> permanent.getId().equals(target.getId())));
        assertThat(gameData.playerGraveyards.values())
                .allSatisfy(graveyard -> assertThat(graveyard).noneMatch(card -> card.getId().equals(target.getCard().getId())));
    }
}
