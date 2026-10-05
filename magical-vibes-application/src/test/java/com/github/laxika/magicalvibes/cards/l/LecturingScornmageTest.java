package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LecturingScornmage.class, HillGiant.class, Shock.class, Blaze.class})
class LecturingScornmageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature adds a +1/+1 counter")
    void reparteeAddsCounter() {
        harness.addToBattlefield(player1, new LecturingScornmage());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        resolveAllTriggers();

        Permanent scornmage = findPermanent(player1, "Lecturing Scornmage");
        assertThat(scornmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new LecturingScornmage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        Permanent scornmage = findPermanent(player1, "Lecturing Scornmage");
        assertThat(scornmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A sorcery targeting an opposing creature triggers even with X equal to zero")
    void zeroDamageSorceryTriggers() {
        harness.addToBattlefield(player1, new LecturingScornmage());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Hill Giant"));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Lecturing Scornmage")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature-targeting instant does not trigger Repartee")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LecturingScornmage());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hill Giant"));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Lecturing Scornmage")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Targeting this creature puts its counter on before the spell resolves")
    void selfTargetingTriggerResolvesBeforeSpell() {
        harness.addToBattlefield(player1, new LecturingScornmage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Lecturing Scornmage"));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lecturing Scornmage")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
