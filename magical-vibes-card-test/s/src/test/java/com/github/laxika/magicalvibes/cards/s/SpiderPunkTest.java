package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderPunk.class, SpiderGwenFreeSpirit.class, GrizzlyBears.class, Cancel.class,
        RodOfRuin.class, Disallow.class, SamiteHealer.class})
class SpiderPunkTest extends BaseCardTest {

    @Test
    void riotChoiceAddsCounterOrHaste() {
        harness.castFromHand(player1, new SpiderPunk(), "{1}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = findPermanent(player1, "Spider-Punk");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
    }

    @Test
    void decliningRiotGivesHaste() {
        harness.castFromHand(player1, new SpiderPunk(), "{1}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = findPermanent(player1, "Spider-Punk");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
    }

    @Test
    void grantsRiotToOtherSpiders() {
        harness.addToBattlefield(player1, new SpiderPunk());
        harness.castFromHand(player1, new SpiderGwenFreeSpirit(), "{2}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = findPermanent(player1, "Spider-Gwen, Free Spirit");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void spellsAndAbilitiesCannotBeCountered() {
        harness.addToBattlefield(player1, new SpiderPunk());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void activatedAbilitiesCannotBeCountered() {
        harness.addToBattlefield(player1, new SpiderPunk());
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        StackEntry ability = gd.stack.getLast();
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, ability.getTargetableId());

        assertThat(gd.stack).contains(ability);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    void damageCannotBePrevented() {
        harness.addToBattlefield(player1, new SpiderPunk());

        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    void grantedRiotCanGiveHaste() {
        harness.addToBattlefield(player1, new SpiderPunk());
        harness.castFromHand(player1, new SpiderGwenFreeSpirit(), "{2}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = findPermanent(player1, "Spider-Gwen, Free Spirit");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
    }

    @Test
    void opposingSpidersDoNotReceiveRiot() {
        harness.addToBattlefield(player1, new SpiderPunk());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SpiderGwenFreeSpirit(), "{2}{R}");
        harness.passBothPriorities();

        Permanent entered = findPermanent(player2, "Spider-Gwen, Free Spirit");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
    }

    @Test
    void nonSpidersDoNotReceiveRiot() {
        harness.addToBattlefield(player1, new SpiderPunk());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isFalse();
    }

    @Test
    void triggeredAbilitiesCannotBeCountered() {
        harness.addToBattlefield(player1, new SpiderPunk());
        addCreatureReady(player2, new SpiderGwenFreeSpirit());
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0));
        StackEntry trigger = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst().orElseThrow();
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, trigger.getTargetableId());

        assertThat(gd.stack).contains(trigger);
    }

    @Test
    void preventionShieldDoesNotStopDamageToPlayer() {
        harness.addToBattlefield(player1, new SpiderPunk());
        addCreatureReady(player1, new SamiteHealer());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void preventionShieldDoesNotStopDamageToCreature() {
        harness.addToBattlefield(player1, new SpiderPunk());
        addCreatureReady(player1, new SamiteHealer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.activateAbility(player1, 1, null, bears.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }
}
