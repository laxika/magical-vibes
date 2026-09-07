package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfBurningAlive.class, GrizzlyBears.class, Forest.class, Shock.class,
        Pacifism.class, PrimordialWurm.class})
class FearOfBurningAliveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each opponent when it enters")
    void dealsFourDamageToEachOpponentOnEntry() {
        Permanent target = addCreatureReady(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new FearOfBurningAlive()));
        addManaForFear();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With delirium, noncombat damage lets it damage a creature controlled by that opponent")
    void damagesOpponentsCreatureForTheAmountDealt() {
        Permanent fear = addCreatureReady(player1, new FearOfBurningAlive());
        Permanent target = addCreatureReady(player2, new PrimordialWurm());
        setDelirium();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(fear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Checks delirium again when the trigger resolves")
    void rechecksDeliriumWhenTriggerResolves() {
        Permanent target = addCreatureReady(player2, new PrimordialWurm());
        setDelirium();
        Permanent fear = addCreatureReady(player1, new FearOfBurningAlive());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(fear.getMarkedDamage()).isZero();
    }

    private void addManaForFear() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
