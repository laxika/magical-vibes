package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SelflessCathar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevouringLight.class, SelflessCathar.class})
class DevouringLightTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target attacking creature")
    void exilesAttackingCreature() {
        Permanent attacker = addAttacker(player1);

        castDevouringLight(TurnStep.DECLARE_ATTACKERS, attacker.getId());

        assertExiled(attacker);
    }

    @Test
    @DisplayName("Exiles the target blocking creature")
    void exilesBlockingCreature() {
        Permanent blocker = addBlocker(player1);

        castDevouringLight(TurnStep.DECLARE_BLOCKERS, blocker.getId());

        assertExiled(blocker);
    }

    @Test
    @DisplayName("Convoke taps a creature to help cast Devouring Light")
    void convokeTapsCreature() {
        Permanent target = addAttacker(player2);
        Permanent convokeCreature = addCreatureReady(player1, new SelflessCathar());
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
    @DisplayName("Can use a newly entered creature for convoke")
    void canConvokeWithSummoningSickCreature() {
        Permanent target = addAttacker(player2);
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
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

    @Test
    @DisplayName("Convoke can pay the entire cost, including both white mana")
    void convokePaysEntireCost() {
        Permanent target = addAttacker(player2);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SelflessCathar());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DevouringLight()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(List.of(first, second, third)).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        harness.passBothPriorities();
        assertExiled(target);
    }

    @Test
    @DisplayName("Tapping a blocking creature for convoke does not make it an illegal target")
    void canConvokeWithTargetedBlocker() {
        Permanent target = addBlocker(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DevouringLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.isBlocking()).isTrue();
        harness.passBothPriorities();
        assertExiled(target);
    }

    @Test
    @DisplayName("Does not exile a target that has left combat before resolution")
    void targetRemovedFromCombatIsIllegalOnResolution() {
        Permanent target = addAttacker(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DevouringLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        target.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        harness.assertInGraveyard(player1, "Devouring Light");
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new SelflessCathar());
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.getId().equals(player1.getId()) ? player2.getId() : player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Player attackerOwner = owner.getId().equals(player1.getId()) ? player2 : player1;
        Permanent attacker = addAttacker(attackerOwner);
        Permanent blocker = addCreatureReady(owner, new SelflessCathar());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }

    private void castDevouringLight(TurnStep step, UUID targetId) {
        harness.forceStep(step);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DevouringLight()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
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
