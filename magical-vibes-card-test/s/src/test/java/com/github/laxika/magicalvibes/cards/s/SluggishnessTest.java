package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.f.FranticSearch;
import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
import com.github.laxika.magicalvibes.cards.t.TragicPoet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sluggishness.class, PlagueBeetle.class, GrimMonolith.class, TragicPoet.class, FranticSearch.class})
class SluggishnessTest extends BaseCardTest {

    @Test
    @DisplayName("An enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new PlagueBeetle());
        harness.setHand(player1, List.of(new Sluggishness()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sluggishness");
        assertThat(aura.getAttachedTo()).isEqualTo(blocker.getId());

        Permanent attacker = addCreatureReady(player1, new PlagueBeetle());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandAfterLeavingBattlefieldForGraveyard() {
        Permanent enchanted = addCreatureReady(player1, new PlagueBeetle());
        Permanent aura = attachSluggishness(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sluggishness");
        harness.assertNotInGraveyard(player1, "Sluggishness");
        harness.assertNotOnBattlefield(player1, "Sluggishness");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByOpponent() {
        Permanent enchanted = addCreatureReady(player2, new PlagueBeetle());
        Sluggishness card = new Sluggishness();
        card.setOwnerId(player1.getId());
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sluggishness");
        harness.assertNotInHand(player2, "Sluggishness");
        harness.assertNotInGraveyard(player1, "Sluggishness");
        harness.assertNotOnBattlefield(player2, "Sluggishness");
    }

    @Test
    @DisplayName("Returns to its owner's hand when the enchanted creature leaves the battlefield")
    void returnsWhenEnchantedCreatureLeavesBattlefield() {
        Permanent enchanted = addCreatureReady(player1, new PlagueBeetle());
        Permanent aura = attachSluggishness(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInHand(player1, aura.getCard().getName());
        harness.assertNotInGraveyard(player1, aura.getCard().getName());
        harness.assertNotOnBattlefield(player1, aura.getCard().getName());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        harness.setHand(player1, List.of(new Sluggishness()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachSluggishness(Player auraController, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new Sluggishness());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    @Test
    @DisplayName("An Aura spell whose target disappears stays in the graveyard")
    void doesNotReturnWhenItsSpellFailsToResolve() {
        Permanent creature = addCreatureReady(player1, new PlagueBeetle());
        harness.setHand(player1, List.of(new Sluggishness()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sluggishness");
        harness.assertNotInHand(player1, "Sluggishness");
        harness.assertNotOnBattlefield(player1, "Sluggishness");
    }

    @Test
    @DisplayName("The return ability returns only the copy that left the battlefield")
    void returnsOnlyItsOwnCopy() {
        Sluggishness otherCopy = new Sluggishness();
        harness.setGraveyard(player1, List.of(otherCopy));
        Permanent creature = addCreatureReady(player1, new PlagueBeetle());
        Permanent aura = attachSluggishness(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(aura.getCard()).doesNotContain(otherCopy);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCopy).doesNotContain(aura.getCard());
    }

    @Test
    @DisplayName("The old return trigger cannot find the Aura after it leaves and reenters the graveyard")
    void doesNotReturnAfterLeavingAndReenteringGraveyard() {
        addCreatureReady(player1, new TragicPoet());
        Permanent creature = addCreatureReady(player1, new PlagueBeetle());
        Permanent aura = attachSluggishness(player1, creature);
        harness.setHand(player1, List.of(new FranticSearch()));
        harness.setLibrary(player1, List.of(new PlagueBeetle(), new PlagueBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(aura.getCard().getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Sluggishness");

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sluggishness");
        harness.assertNotInHand(player1, "Sluggishness");
    }
}
