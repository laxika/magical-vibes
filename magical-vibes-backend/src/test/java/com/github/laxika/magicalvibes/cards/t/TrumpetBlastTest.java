package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrumpetBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Trumpet Blast has correct effects configured")
    void hasCorrectEffects() {
        TrumpetBlast card = new TrumpetBlast();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(BoostAllCreaturesEffect.class);
        BoostAllCreaturesEffect boost = (BoostAllCreaturesEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.filter()).isInstanceOf(PermanentIsAttackingPredicate.class);
    }

    @Test
    @DisplayName("Trumpet Blast boosts attacking creatures with +2/+0")
    void boostsAttackingCreatures() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TrumpetBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        // Attacking creature gets +2/+0
        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        // Non-attacking creature is unaffected
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trumpet Blast effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new TrumpetBlast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
