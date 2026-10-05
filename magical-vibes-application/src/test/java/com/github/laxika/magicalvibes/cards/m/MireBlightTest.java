package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MireBlight.class, GrizzlyBears.class, HillGiant.class, Shock.class, Swamp.class,
        AuraGraft.class, IntoTheRoil.class})
class MireBlightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mire Blight attaches it to the target creature")
    void resolvingAttachesToCreature() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mire Blight")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Mire Blight targeting a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Non-lethal damage to the enchanted creature destroys it")
    void damageDestroysEnchantedCreature() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Mire Blight"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Mire Blight");
    }

    @Test
    @DisplayName("Damage to a different creature does not destroy the enchanted one")
    void damageToOtherCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, other.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Mire Blight");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Moving Mire Blight in response does not change the creature destroyed")
    void movingAuraDoesNotChangeTriggeredCreature() {
        Permanent original = addCreatureReady(player2, new HillGiant());
        Permanent destination = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Mire Blight");

        harness.setHand(player1, List.of(new Shock(), new AuraGraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, original.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mire Blight");
    }

    @Test
    @DisplayName("Returning Mire Blight to hand does not stop its pending destroy trigger")
    void triggerSurvivesAuraLeavingBattlefield() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Mire Blight");

        harness.setHand(player1, List.of(new Shock(), new IntoTheRoil()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Mire Blight");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Nonlethal combat damage triggers destruction of the enchanted creature")
    void combatDamageDestroysEnchantedCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mire Blight");
    }
}
