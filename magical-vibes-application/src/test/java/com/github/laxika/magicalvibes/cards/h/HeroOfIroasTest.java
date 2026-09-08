package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BondsOfFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeroOfIroasTest extends BaseCardTest {

    @Test
    @DisplayName("Aura spells cost {1} less to cast")
    void auraSpellsCostOneLess() {
        harness.addToBattlefield(player1, new HeroOfIroas());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        harness.castEnchantment(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a spell that targets Hero of Iroas puts a +1/+1 counter on it")
    void castingSpellThatTargetsHeroPutsCounterOnIt() {
        harness.addToBattlefield(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID heroId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castInstant(player1, 0, heroId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hero = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Hero of Iroas")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent hero = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Hero of Iroas does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new HeroOfIroas());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID heroId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castInstant(player2, 0, heroId);
        harness.passBothPriorities();

        Permanent hero = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
