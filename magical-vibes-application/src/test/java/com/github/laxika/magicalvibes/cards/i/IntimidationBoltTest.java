package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class IntimidationBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the targeted creature, killing a 2/2")
    void dealsThreeDamageToTargetCreature() {
        Permanent bear = addReadyCreature(player2, new GrizzlyBears());
        castBolt(player1, bear.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Other creatures can't attack, but the targeted creature that survived still can")
    void otherCreaturesCantAttackButTargetSurvivorCan() {
        Permanent target = addReadyCreature(player1, new GiantSpider());   // 2/4, survives 3 damage
        Permanent other = addReadyCreature(player1, new GrizzlyBears());   // 2/2, not targeted
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
        Permanent target = addReadyCreature(player1, new GrizzlyBears());  // 2/2, dies to 3 damage
        addReadyCreature(player1, new GrizzlyBears());                     // survivor, not targeted
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
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        // Lock every creature except some unrelated (now-dead) target.
        gd.otherCreaturesCantAttackExemptCreatureIds.add(UUID.randomUUID());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .doesNotContain(indexOf(player1, bear));

        // player1 -> player2 -> player1: the transition clears the restriction.
        advanceTurn();
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.otherCreaturesCantAttackExemptCreatureIds).isEmpty();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, bear));
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyCreature(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new IntimidationBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castBolt(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new IntimidationBolt()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
