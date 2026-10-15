package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfEndurance;
import com.github.laxika.magicalvibes.cards.b.BoltOfKeranos;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SatyrFiredancer.class, Shock.class, GrizzlyBears.class,
        BoltOfKeranos.class, NyxbornWolf.class, ArchetypeOfEndurance.class})
class SatyrFiredancerTest extends BaseCardTest {

    @Test
    @DisplayName("Instant damage chooses the opponent's creature before the trigger resolves")
    void damagesCreatureControlledByDamagedOpponent() {
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Sorcery damage triggers Firedancer for the actual damage dealt")
    void sorceryDamageTriggers() {
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BoltOfKeranos()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Nyxborn Wolf");
    }

    @Test
    @DisplayName("Firedancer cannot target an opponent's hexproof creatures")
    void cannotTargetHexproofCreatures() {
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArchetypeOfEndurance());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BoltOfKeranos()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Damage to your own life total does not trigger Firedancer")
    void damageToControllerDoesNotTrigger() {
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage to a creature does not trigger Firedancer")
    void creatureDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger your Firedancer")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SatyrFiredancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }
}
