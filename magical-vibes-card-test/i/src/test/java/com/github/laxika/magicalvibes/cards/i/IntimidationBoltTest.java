package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SewnEyeDrake;
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

@CardUsed({IntimidationBolt.class, GiantSpider.class, GrizzlyBears.class, SewnEyeDrake.class})
class IntimidationBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the targeted creature, killing a 2/2")
    void dealsThreeDamageToTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castBolt(player1, bear.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Other creatures can't attack, but the targeted creature that survived still can")
    void otherCreaturesCantAttackButTargetSurvivorCan() {
        Permanent target = addCreatureReady(player1, new GiantSpider());   // 2/4, survives 3 damage
        Permanent other = addCreatureReady(player1, new GrizzlyBears());   // 2/2, not targeted
        castBolt(player1, target.getId());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        List<Integer> attackable = harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId());

        int targetIndex = indexOf(player1, target);
        int otherIndex = indexOf(player1, other);
        assertThat(attackable).contains(targetIndex);
        assertThat(attackable).doesNotContain(otherIndex);
    }

    @Test
    @DisplayName("If the targeted creature dies to the damage, no creature can attack this turn")
    void whenTargetDiesNoCreatureCanAttack() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());  // 2/2, dies to 3 damage
        addCreatureReady(player1, new GrizzlyBears());                     // survivor, not targeted
        castBolt(player1, target.getId());

        // The targeted 2/2 is gone; only the untargeted bear remains, and it still can't attack.
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The restriction clears at the turn transition and creatures can attack again")
    void restrictionClearsNextTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castBolt(player1, target.getId());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .doesNotContain(indexOf(player1, bear));

        // player1 -> player2 -> player1: the transition clears the restriction.
        advanceTurn();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, bear));
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IntimidationBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Marks exactly 3 damage on a surviving target")
    void survivingTargetTakesThreeDamage() {
        Permanent target = addCreatureReady(player2, new GiantSpider());
        castBolt(player1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A creature with haste entering after resolution cannot attack")
    void laterHastyCreatureCannotAttack() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castBolt(player1, target.getId());
        Permanent drake = harness.enterBattlefieldAndReturn(player1, new SewnEyeDrake());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .doesNotContain(indexOf(player1, drake));
    }

    @Test
    @DisplayName("An illegal target prevents both damage and the attack restriction")
    void removedTargetDoesNotRestrictAttacks() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IntimidationBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Intimidation Bolt");
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, attacker));
    }

    @Test
    @DisplayName("Two Bolts targeting different surviving creatures prevent both from attacking")
    void differentTargetsDoNotShareAnExemption() {
        Permanent first = addCreatureReady(player1, new GiantSpider());
        Permanent second = addCreatureReady(player1, new GiantSpider());
        castBolt(player1, first.getId());
        castBolt(player1, second.getId());

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving after attackers are declared does not remove surviving attackers from combat")
    void alreadyDeclaredAttackersRemainAttacking() {
        Permanent target = addCreatureReady(player2, new GiantSpider());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(indexOf(player1, attacker))));
        castBolt(player1, target.getId());

        assertThat(attacker.isAttacking()).isTrue();
    }

    private void castBolt(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new IntimidationBolt()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
