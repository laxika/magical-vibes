package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
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

@CardUsed({PearlShard.class, Frogmite.class, PyriteSpellbomb.class})
class PearlShardTest extends BaseCardTest {

    @Test
    @DisplayName("The white activation cannot be paid with colorless mana")
    void whiteActivationRequiresWhiteMana() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Pearl Shard cannot activate its other payment option")
    void bothPaymentOptionsRequireTapping() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature artifact is not a legal prevention target")
    void cannotTargetNoncreatureArtifact() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, shard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The white activation protects an opponent from only the next 2 noncombat damage")
    void whiteActivationShieldIsConsumedByNoncombatDamage() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new PyriteSpellbomb());
        harness.addToBattlefield(player1, new PyriteSpellbomb());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The generic activation prevents noncombat damage to a creature")
    void genericActivationPreventsNoncombatDamageToCreature() {
        harness.addToBattlefield(player1, new PearlShard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Frogmite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new PyriteSpellbomb());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unused prevention shield expires at the end of the turn")
    void unusedShieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.addToBattlefield(player2, new PyriteSpellbomb());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The {3} activation prevents the next 2 damage to a player")
    void genericActivationPreventsDamageToPlayer() {
        Permanent pearlShard = harness.addToBattlefieldAndReturn(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, player1.getId());
        assertThat(pearlShard.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Frogmite());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The prevention shield only prevents the next 2 damage")
    void preventionShieldOnlyPreventsNextTwoDamage() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Frogmite());
        attacker.setPowerModifier(1);
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The {W} activation prevents the next 2 damage to a creature")
    void whiteActivationPreventsDamageToCreature() {
        Permanent pearlShard = harness.addToBattlefieldAndReturn(player1, new PearlShard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = addCreatureReady(player2, new Frogmite());
        Permanent attacker = addCreatureReady(player1, new Frogmite());
        attacker.setAttacking(true);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(pearlShard.isTapped()).isTrue();
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}
