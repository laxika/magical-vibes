package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
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

@CardUsed({SleepersGuile.class, GiantCockroach.class, Crawlspace.class})
class SleepersGuileTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sleeper's Guile attaches it and gives the enchanted creature fear")
    void resolvingAttachesAndGrantsFear() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sleeper's Guile");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Sleeper's Guile can enchant a creature controlled by an opponent")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sleeper's Guile");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Sleeper's Guile gives fear only to the enchanted creature")
    void grantsFearOnlyToEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Sleeper's Guile returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SleepersGuile());
        aura.setAttachedTo(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sleeper's Guile");
        harness.assertNotInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotOnBattlefield(player1, "Sleeper's Guile");
    }

    @Test
    @DisplayName("Sleeper's Guile returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByOpponent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        SleepersGuile card = new SleepersGuile();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());
        gd.stolenCreatures.put(aura.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sleeper's Guile");
        harness.assertNotInHand(player2, "Sleeper's Guile");
        harness.assertNotInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotOnBattlefield(player2, "Sleeper's Guile");
    }

    @Test
    @DisplayName("Sleeper's Guile returns after its enchanted creature dies")
    void returnsAfterEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotInHand(player1, "Sleeper's Guile");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sleeper's Guile");
        harness.assertNotInGraveyard(player1, "Sleeper's Guile");
        harness.assertInGraveyard(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("Sleeper's Guile does not return when its spell's target disappears")
    void doesNotReturnWhenAuraSpellFailsToResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sleeper's Guile");
        harness.assertNotInHand(player1, "Sleeper's Guile");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sleeper's Guile returns only itself, not other copies in graveyards")
    void returnsOnlyTheAuraThatDied() {
        SleepersGuile other = new SleepersGuile();
        harness.setGraveyard(player1, List.of(other));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SleepersGuile());
        aura.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Sleeper's Guile cannot return after leaving the graveyard before its trigger resolves")
    void doesNotReturnIfNoLongerInGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SleepersGuile());
        aura.setAttachedTo(creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.assertInGraveyard(player1, "Sleeper's Guile");

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(aura.getCard()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Sleeper's Guile");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(aura.getCard());
    }

    @Test
    @DisplayName("An old return trigger cannot return Sleeper's Guile after it leaves and reenters the graveyard")
    void oldTriggerDoesNotReturnNewGraveyardObject() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        SleepersGuile card = new SleepersGuile();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, card);
        aura.setAttachedTo(creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.assertInGraveyard(player1, "Sleeper's Guile");

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card));
        gd.markGraveyardEntry(card);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Sleeper's Guile");
        harness.assertInGraveyard(player1, "Sleeper's Guile");
    }

    @Test
    @DisplayName("Sleeper's Guile cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        harness.setHand(player1, List.of(new SleepersGuile()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
