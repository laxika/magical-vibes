package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourtlyProvocateur.class, WalkingCorpse.class, Forest.class})
class CourtlyProvocateurTest extends BaseCardTest {

    private static final int ABILITY_MUST_ATTACK = 0;
    private static final int ABILITY_MUST_BLOCK = 1;

    @Test
    @DisplayName("First ability forces the target to attack this turn without dictating whom it attacks")
    void mustAttackAbility() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustAttackIfAbleEffect.class)).isTrue();
        assertThat(gd.mustAttackDefender(target.getId())).isNull();
    }

    @Test
    @DisplayName("First ability can target a creature its controller controls")
    void mustAttackCanTargetOwnCreature() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustAttackIfAbleEffect.class)).isTrue();
    }

    @Test
    @DisplayName("Must-attack requirement wears off at end of turn")
    void mustAttackWearsOff() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustAttackIfAbleEffect.class)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustAttackIfAbleEffect.class)).isFalse();
    }

    @Test
    @DisplayName("Second ability forces the target to be declared as a blocker when able")
    void mustBlockAbility() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustBlockEachCombatEffect.class)).isTrue();

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Second ability requirement is satisfied by declaring the block")
    void mustBlockSatisfiedByBlocking() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Each ability imposes only its own requirement, not the other one")
    void abilitiesImposeOnlyTheirOwnRequirement() {
        addCreatureReady(player1, new CourtlyProvocateur());
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attackTarget = addCreatureReady(player2, new WalkingCorpse());
        Permanent blockTarget = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, attackTarget.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, ABILITY_MUST_BLOCK, null, blockTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.hasFloatingEffectOnPermanent(attackTarget.getId(), MustAttackIfAbleEffect.class)).isTrue();
        assertThat(gd.hasFloatingEffectOnPermanent(attackTarget.getId(), MustBlockEachCombatEffect.class)).isFalse();
        assertThat(gd.hasFloatingEffectOnPermanent(attackTarget.getId(), MustBeBlockedIfAbleEffect.class)).isFalse();
        assertThat(gd.hasFloatingEffectOnPermanent(attackTarget.getId(), MustBeBlockedByAllCreaturesEffect.class)).isFalse();

        assertThat(gd.hasFloatingEffectOnPermanent(blockTarget.getId(), MustBlockEachCombatEffect.class)).isTrue();
        assertThat(gd.hasFloatingEffectOnPermanent(blockTarget.getId(), MustAttackIfAbleEffect.class)).isFalse();
        assertThat(gd.mustAttackDefender(blockTarget.getId())).isNull();
        assertThat(gd.hasFloatingEffectOnPermanent(blockTarget.getId(), MustBeBlockedIfAbleEffect.class)).isFalse();
        assertThat(gd.hasFloatingEffectOnPermanent(blockTarget.getId(), MustBeBlockedByAllCreaturesEffect.class)).isFalse();
    }

    @Test
    @DisplayName("Abilities can't target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A creature able to attack cannot be omitted after the first ability resolves")
    void mustAttackIsEnforced() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> declareAttackers(List.of(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped creature is not forced to attack")
    void tappedCreatureNeedNotAttack() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());
        target.tap();

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Summoning sickness prevents a forced attack")
    void summoningSickCreatureNeedNotAttack() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());
        target.setSummoningSick(true);

        harness.activateAbility(player1, 0, ABILITY_MUST_ATTACK, null, target.getId());
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped creature is not forced to block")
    void tappedCreatureNeedNotBlock() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.tap();

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();
        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Summoning sickness does not excuse a creature from blocking")
    void summoningSickCreatureMustBlock() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.setSummoningSick(true);

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();
        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The block ability can target an own creature and expires during cleanup")
    void ownCreatureBlockRequirementWearsOff() {
        addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, ABILITY_MUST_BLOCK, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustBlockEachCombatEffect.class)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPermanent(target.getId(), MustBlockEachCombatEffect.class)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {ABILITY_MUST_ATTACK, ABILITY_MUST_BLOCK})
    @DisplayName("Both abilities tap their source as a cost")
    void abilitiesRequireUntappedSource(int abilityIndex) {
        Permanent source = addCreatureReady(player1, new CourtlyProvocateur());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @ParameterizedTest
    @ValueSource(ints = {ABILITY_MUST_ATTACK, ABILITY_MUST_BLOCK})
    @DisplayName("Summoning sickness prevents activation of both tap abilities")
    void summoningSickSourceCannotActivate(int abilityIndex) {
        Permanent source = addCreatureReady(player1, new CourtlyProvocateur());
        source.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new WalkingCorpse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }
}
