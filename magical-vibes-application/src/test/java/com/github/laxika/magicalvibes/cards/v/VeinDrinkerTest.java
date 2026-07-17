package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeinDrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Fight deals mutual power damage and gains a +1/+1 counter when the target dies")
    void fightKillsTargetAndGainsCounter() {
        Permanent drinker = addReadyDrinker(player1);
        Permanent target = addReadyBears(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        // Pass 1: fight resolves — 4 damage kills the 2/2, death trigger fires
        harness.passBothPriorities();
        // Pass 2: ON_DAMAGED_CREATURE_DIES trigger resolves
        harness.passBothPriorities();

        // Target destroyed by lethal power damage
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        // Vein Drinker took 2 reciprocal damage but survives
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drinker);
        assertThat(drinker.getMarkedDamage()).isEqualTo(2);
        // +1/+1 counter from the damaged creature dying
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter when the fought creature survives")
    void noCounterWhenTargetSurvives() {
        Permanent drinker = addReadyDrinker(player1);
        GrizzlyBears bigBearsCard = new GrizzlyBears();
        bigBearsCard.setToughness(5);
        Permanent target = new Permanent(bigBearsCard);
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Target survives 4 damage on 5 toughness — no death, no counter
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(drinker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyDrinker(player1);
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent addReadyDrinker(Player player) {
        Permanent perm = new Permanent(new VeinDrinker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
