package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoonOfSafety.class, Combust.class, AirElemental.class, GrizzlyBears.class, Murder.class, Shock.class})
class BoonOfSafetyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a shield counter on the target and scries 1")
    void putsShieldCounterAndScries() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBoonOfSafety(creature);

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("A shield counter prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoonOfSafety(creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A shield counter replaces destruction")
    void shieldCounterReplacesDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoonOfSafety(creature);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("An unpreventable damage event still removes a shield counter")
    void unpreventableDamageRemovesShieldCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castBoonOfSafety(creature);

        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertNotOnBattlefield(player1, "Air Elemental");
    }

    private void castBoonOfSafety(Permanent target) {
        harness.setHand(player1, List.of(new BoonOfSafety()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
