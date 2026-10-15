package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.DauntlessOnslaught;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FabledHero.class, Shock.class, GiantGrowth.class, DauntlessOnslaught.class})
class FabledHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Fabled Hero puts a +1/+1 counter on it")
    void castingSpellThatTargetsHeroPutsCounterOnIt() {
        harness.addToBattlefield(player1, new FabledHero());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID heroId = harness.getPermanentId(player1, "Fabled Hero");
        harness.castAndResolveInstant(player1, 0, heroId);
        harness.passBothPriorities();

        Permanent hero = findPermanent(player1, "Fabled Hero");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Fabled Hero")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new FabledHero());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent hero = findPermanent(player1, "Fabled Hero");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Fabled Hero does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new FabledHero());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID heroId = harness.getPermanentId(player1, "Fabled Hero");
        harness.castAndResolveInstant(player2, 0, heroId);

        Permanent hero = findPermanent(player1, "Fabled Hero");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heroic resolves before the spell that triggered it")
    void counterIsAddedBeforeTargetingSpellResolves() {
        harness.addToBattlefield(player1, new FabledHero());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent hero = findPermanent(player1, "Fabled Hero");

        harness.castInstant(player1, 0, hero.getId());
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hero);
    }

    @Test
    @DisplayName("A spell targeting two Heroes triggers each exactly once")
    void multiTargetSpellTriggersEachTargetedHeroOnce() {
        harness.addToBattlefield(player1, new FabledHero());
        harness.addToBattlefield(player1, new FabledHero());
        Permanent first = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the targeted Hero triggers, and successive casts each add a counter")
    void successiveSpellsOnlyTriggerTheTargetedInstance() {
        harness.addToBattlefield(player1, new FabledHero());
        harness.addToBattlefield(player1, new FabledHero());
        Permanent targeted = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent other = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new DauntlessOnslaught(), new DauntlessOnslaught()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, List.of(targeted.getId()));
            harness.passBothPriorities();
            assertThat(targeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i + 1);
            assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }

    @Test
    @DisplayName("A Heroic counter increases damage in both double strike damage steps")
    void heroicCounterIncreasesBothCombatDamageSteps() {
        harness.addToBattlefield(player1, new FabledHero());
        Permanent hero = findPermanent(player1, "Fabled Hero");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, hero.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 20);
        hero.setSummoningSick(false);
        hero.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
