package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.g.Galvanize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnocentBystander.class, Shock.class, LightningBolt.class, Galvanize.class})
class InnocentBystanderTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when dealt 3 or more damage")
    void investigatesWhenDealtAtLeastThreeDamage() {
        harness.addToBattlefield(player2, new InnocentBystander());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Innocent Bystander"));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not investigate when dealt less than 3 damage")
    void doesNotInvestigateWhenDealtLessThanThreeDamage() {
        harness.addToBattlefield(player2, new InnocentBystander());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Innocent Bystander"));

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Two combat damage does not trigger investigate")
    void combatDamageBelowThresholdDoesNotTrigger() {
        addCreatureReady(player1, new InnocentBystander());
        harness.addToBattlefield(player2, new InnocentBystander());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Innocent Bystander");
        harness.assertInGraveyard(player2, "Innocent Bystander");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous damage from two blockers investigates once")
    void simultaneousCombatDamageInvestigatesOnce() {
        addCreatureReady(player1, new InnocentBystander());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new InnocentBystander());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new InnocentBystander());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 0));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Innocent Bystander");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Five damage investigates exactly once after lethal damage")
    void damageAboveThresholdInvestigatesOnce() {
        harness.addToBattlefield(player2, new InnocentBystander());
        harness.setHand(player1, List.of(new Galvanize()));
        harness.addMana(player1, ManaColor.RED, 2);
        gd.cardsDrawnThisTurn.put(player1.getId(), 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Innocent Bystander"));
        harness.assertInGraveyard(player2, "Innocent Bystander");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void investigatedClueDrawsCard() {
        harness.addToBattlefield(player2, new InnocentBystander());
        harness.setHand(player1, List.of(new Galvanize()));
        harness.setHand(player2, List.of());
        InnocentBystander drawnCard = new InnocentBystander();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Innocent Bystander"));
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }
}
