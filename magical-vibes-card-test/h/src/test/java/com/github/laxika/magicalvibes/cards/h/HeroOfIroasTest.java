package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BondsOfFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.n.NyxbornShieldmate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroOfIroas.class, BondsOfFaith.class, GrizzlyBears.class, GiantGrowth.class,
        Shock.class, NyxbornShieldmate.class})
class HeroOfIroasTest extends BaseCardTest {

    @Test
    @DisplayName("Aura spells cost {1} less to cast")
    void auraSpellsCostOneLess() {
        harness.addToBattlefield(player1, new HeroOfIroas());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a spell that targets Hero of Iroas puts a +1/+1 counter on it")
    void castingSpellThatTargetsHeroPutsCounterOnIt() {
        harness.addToBattlefield(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID heroId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castAndResolveInstant(player1, 0, heroId);
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

        harness.castAndResolveInstant(player1, 0, player2.getId());

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
        harness.castAndResolveInstant(player2, 0, heroId);

        Permanent hero = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heroic resolves before the targeting spell and can save the Hero from lethal damage")
    void heroicResolvesBeforeTargetingSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hero.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hero);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bestow receives the Aura discount and triggers heroic on its target")
    void bestowReceivesAuraDiscountAndTriggersHeroic() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castWithAlternateCost(player1, 0, hero.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != hero)
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(hero.getId());
    }

    @Test
    @DisplayName("Multiple Heroes stack their Aura discounts but only the targeted Hero triggers")
    void multipleHeroesStackDiscounts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Aura discounts do not pay the colored portion of a bestow cost")
    void auraDiscountDoesNotRemoveColoredCost() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        harness.addToBattlefield(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, hero.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Aura gets no discount and does not trigger heroic")
    void opponentAuraGetsNoDiscount() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NyxbornShieldmate()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castWithAlternateCost(player2, 0, hero.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a non-Aura creature does not receive the Aura discount")
    void nonAuraCreatureGetsNoDiscount() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfIroas());
        harness.setHand(player1, List.of(new HeroOfIroas()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
