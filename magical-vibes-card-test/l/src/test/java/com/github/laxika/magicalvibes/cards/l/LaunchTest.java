package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GoblinCadets;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.Rescind;
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

@CardUsed({Launch.class, GoblinCadets.class, Island.class, Rescind.class})
class LaunchTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Launch attaches it and gives the enchanted creature flying")
    void resolvingAttachesAndGrantsFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinCadets());
        harness.setHand(player1, List.of(new Launch()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Launch");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Launch can enchant a creature an opponent controls")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinCadets());
        harness.setHand(player1, List.of(new Launch()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Launch");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Launch returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinCadets());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Launch());
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Launch");
        harness.assertNotInGraveyard(player1, "Launch");
        harness.assertNotOnBattlefield(player1, "Launch");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Launch returns to its owner's hand when another player controls it")
    void returnsToOwnerHandWhenControlledByAnotherPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinCadets());
        Launch launch = new Launch();
        launch.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, launch);
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Launch");
        harness.assertNotInHand(player2, "Launch");
        harness.assertNotInGraveyard(player1, "Launch");
        harness.assertNotInGraveyard(player2, "Launch");
    }

    @Test
    @DisplayName("Launch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Launch()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Launch returns after its enchanted creature leaves the battlefield")
    void returnsWhenEnchantedCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinCadets());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Launch());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Launch");
        harness.assertNotInHand(player1, "Launch");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Launch");
        harness.assertNotInGraveyard(player1, "Launch");
    }

    @Test
    @DisplayName("Launch does not return when its spell loses its target")
    void doesNotReturnWhenAuraSpellFailsToResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinCadets());
        harness.setHand(player1, List.of(new Launch()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Rescind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Launch");
        harness.assertNotInHand(player1, "Launch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The return trigger cannot return Launch after it leaves and reenters the graveyard")
    void doesNotReturnNewGraveyardObject() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinCadets());
        Launch launch = new Launch();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, launch);
        aura.setAttachedTo(creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        // Model an intervening graveyard-to-hand return followed by a discard before resolution.
        gd.playerGraveyards.get(player1.getId()).remove(launch);
        harness.setHand(player1, List.of(launch));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(launch));
        gd.markGraveyardEntry(launch);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Launch");
        harness.assertNotInHand(player1, "Launch");
    }
}
