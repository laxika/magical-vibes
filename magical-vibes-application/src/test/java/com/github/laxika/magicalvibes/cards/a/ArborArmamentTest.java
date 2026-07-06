package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class ArborArmamentTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving Arbor Armament puts a +1/+1 counter and grants reach to target creature")
    void putsCounterAndGrantsReach() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("+1/+1 counter persists but reach expires at end of turn")
    void counterPersistsReachExpires() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        Permanent ownCreature = addCreature(player1);
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1);
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Arbor Armament fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
