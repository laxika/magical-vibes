package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRedTerror.class, LightningBolt.class, Pyroclasm.class, GrizzlyBears.class,
        BalduvianBarbarians.class, Humble.class})
class TheRedTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a red spell you control deals damage")
    void redSpellYouControlDealsDamage() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets only one counter when one red source damages multiple permanents")
    void oneCounterForOneDamageEvent() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger from a nonred source you control")
    void nonredSourceDoesNotTrigger() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger from a red source controlled by an opponent")
    void opponentsRedSourceDoesNotTrigger() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        Permanent attacker = addCreatureReady(player2, new BalduvianBarbarians());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers from The Red Terror's own combat damage")
    void ownCombatDamageTriggers() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        terror.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each distinct red source dealing simultaneous combat damage triggers separately")
    void distinctCombatSourcesTriggerSeparately() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        Permanent attacker = addCreatureReady(player1, new BalduvianBarbarians());
        terror.setAttacking(true);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Red spell damage to a creature you control triggers")
    void redSpellDamageToOwnCreatureTriggers() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger after losing all abilities")
    void losingAllAbilitiesStopsDamageTrigger() {
        Permanent terror = addCreatureReady(player1, new TheRedTerror());
        harness.setHand(player1, List.of(new Humble(), new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, terror.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(terror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
