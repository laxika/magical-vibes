package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.FracturingGust;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YokeOfTheDamned.class, GrizzlyBears.class, HillGiant.class, Shock.class, Swamp.class,
        Naturalize.class, Unsummon.class, FracturingGust.class, Ornithopter.class})
class YokeOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Yoke of the Damned attaches it to the target creature")
    void resolvingAttachesToCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new YokeOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Yoke of the Damned")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Yoke of the Damned targeting a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new HillGiant()); // valid target so spell is playable
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new YokeOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("When another creature dies, the enchanted creature is destroyed")
    void anotherCreatureDyingDestroysEnchantedCreature() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3, the enchanted creature
        addYokeAttachedTo(player1, "Hill Giant");
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, dies to Shock

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId); // Resolve Shock — Grizzly Bears dies

        // Yoke's death trigger should now be on the stack.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // Resolve the trigger

        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("When the enchanted creature itself dies, the trigger has nothing to destroy")
    void enchantedCreatureDyingLeavesNothingToDestroy() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2, enchanted and dies to Shock
        addYokeAttachedTo(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // The creature and its now-orphaned Aura are both gone; nothing else is affected.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Yoke of the Damned");
        harness.assertInGraveyard(player1, "Yoke of the Damned");
    }

    @Test
    @DisplayName("A creature controlled by the Aura controller dying destroys an opposing enchanted creature")
    void controllersCreatureDyingDestroysOpponentsEnchantedCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new YokeOfTheDamned());
        aura.setAttachedTo(giant.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Yoke of the Damned");
    }

    @Test
    @DisplayName("Nonlethal damage does not trigger Yoke")
    void nonlethalDamageDoesNotTrigger() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addYokeAttachedTo(player1, "Hill Giant");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Yoke of the Damned");
    }

    @Test
    @DisplayName("Returning another creature to hand does not trigger Yoke")
    void returningCreatureToHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new HillGiant());
        addYokeAttachedTo(player1, "Hill Giant");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop its triggered ability")
    void destroyingAuraInResponseDoesNotStopTrigger() {
        harness.addToBattlefield(player1, new HillGiant());
        addYokeAttachedTo(player1, "Hill Giant");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Naturalize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Yoke of the Damned"));
        harness.assertInGraveyard(player1, "Yoke of the Damned");
        harness.assertOnBattlefield(player1, "Hill Giant");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Yoke triggers when it and an artifact creature are destroyed simultaneously")
    void simultaneousAuraAndCreatureDestructionStillTriggers() {
        harness.addToBattlefield(player1, new HillGiant());
        addYokeAttachedTo(player1, "Hill Giant");
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FracturingGust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Yoke of the Damned");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private void addYokeAttachedTo(Player owner, String creatureName) {
        Permanent creature = findPermanent(owner, creatureName);
        Permanent aura = harness.addToBattlefieldAndReturn(owner, new YokeOfTheDamned());
        aura.setAttachedTo(creature.getId());
    }
}
