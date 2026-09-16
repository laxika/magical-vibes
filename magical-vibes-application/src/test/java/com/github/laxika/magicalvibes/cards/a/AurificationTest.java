package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrandColiseum;
import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aurification.class, Forest.class, GrandColiseum.class, GoblinSharpshooter.class, Naturalize.class})
class AurificationTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that deals damage to you gets a gold counter and becomes a Wall with defender")
    void marksCreatureThatDealsDamage() {
        harness.addToBattlefield(player2, new Aurification());
        Permanent shooter = addCreatureReady(player1, new GoblinSharpshooter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(shooter.getCounterCount(CounterType.GOLD)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, shooter)).contains(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, shooter, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Combat damage from a creature also puts a gold counter on it")
    void marksCreatureThatDealsCombatDamage() {
        harness.addToBattlefield(player2, new Aurification());
        Permanent attacker = addCreatureReady(player1, new GoblinSharpshooter());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(attacker.getCounterCount(CounterType.GOLD)).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature permanent that deals damage does not get a gold counter")
    void doesNotMarkNoncreatureDamageSource() {
        harness.addToBattlefield(player1, new Aurification());
        Permanent coliseum = harness.addToBattlefieldAndReturn(player1, new GrandColiseum());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(coliseum.getCounterCount(CounterType.GOLD)).isZero();
    }

    @Test
    @DisplayName("A creature with a gold counter cannot attack")
    void goldCounterCreatureCannotAttack() {
        harness.addToBattlefield(player2, new Aurification());
        Permanent creature = addCreatureReady(player1, new GoblinSharpshooter());
        creature.setCounterCount(CounterType.GOLD, 1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Leaving the battlefield removes gold counters from creatures but not other permanents")
    void leavesRemovesGoldCountersFromCreatures() {
        Permanent aurification = harness.addToBattlefieldAndReturn(player1, new Aurification());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GoblinSharpshooter());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GoblinSharpshooter());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownCreature.setCounterCount(CounterType.GOLD, 1);
        opposingCreature.setCounterCount(CounterType.GOLD, 2);
        land.setCounterCount(CounterType.GOLD, 3);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, aurification.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.GOLD)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.GOLD)).isZero();
        assertThat(land.getCounterCount(CounterType.GOLD)).isEqualTo(3);
    }
}
