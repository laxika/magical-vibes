package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
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
        Pacifism.class, SpinedWurm.class, TrollAscetic.class})
class FearOfBurningAliveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each opponent when it enters")
    void dealsFourDamageToEachOpponentOnEntry() {
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.castFromHand(player1, new FearOfBurningAlive(), "{4}{R}{R}");
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
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        setDelirium();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(fear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Checks delirium again when the trigger resolves")
    void rechecksDeliriumWhenTriggerResolves() {
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        setDelirium();
        Permanent fear = addCreatureReady(player1, new FearOfBurningAlive());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(fear.getMarkedDamage()).isZero();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }

    @Test
    @DisplayName("Its own entry damage triggers delirium")
    void entryDamageTriggersDelirium() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setDelirium();
        harness.castFromHand(player1, new FearOfBurningAlive(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(target.getMarkedDamage()).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when delirium is absent at the damage event")
    void resolvingShockCannotSupplyTheFourthTypeForItsOwnDamage() {
        addCreatureReady(player1, new FearOfBurningAlive());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Pacifism()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's hexproof creature cannot be the delirium target")
    void cannotTargetOpponentsHexproofCreature() {
        addCreatureReady(player1, new FearOfBurningAlive());
        Permanent troll = addCreatureReady(player2, new TrollAscetic());
        setDelirium();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Damage to its controller does not trigger delirium")
    void damageToControllerDoesNotTrigger() {
        Permanent fear = addCreatureReady(player1, new FearOfBurningAlive());
        addCreatureReady(player2, new SpinedWurm());
        setDelirium();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(fear.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Damage from an opponent's source does not trigger delirium")
    void opponentsSourceDoesNotTrigger() {
        addCreatureReady(player1, new FearOfBurningAlive());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        setDelirium();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Combat damage does not trigger delirium")
    void combatDamageDoesNotTrigger() {
        addCreatureReady(player1, new FearOfBurningAlive());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        target.setTapped(true);
        setDelirium();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An enchantment creature supplies two types for delirium")
    void countsBothTypesOfAnEnchantmentCreature() {
        addCreatureReady(player1, new FearOfBurningAlive());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.setGraveyard(player1, List.of(new FearOfBurningAlive(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }
}
