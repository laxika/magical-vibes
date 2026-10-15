package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.cards.i.InvokeTheDivine;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealAway.class, CabalEvangel.class, InvokeTheDivine.class, BlinkOfAnEye.class})
class SealAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Seal Away can be cast without any legal ETB target")
    void canCastWithoutLegalTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Seal Away");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows Seal Away during the opponent's turn")
    void canCastDuringOpponentsTurn() {
        Permanent creature = addTappedOpponentCreature();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Seal Away");
        harness.assertNotOnBattlefield(player2, "Cabal Evangel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("A creature untapped before the ETB resolves is not exiled")
    void untappedTargetIsIllegalOnResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = addTappedOpponentCreature();
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        creature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Seal Away");
        harness.assertOnBattlefield(player2, "Cabal Evangel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing Seal Away before its ETB resolves prevents exile")
    void sourceLeavesBeforeTriggerResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = addTappedOpponentCreature();
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Seal Away"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Seal Away");
        harness.assertOnBattlefield(player2, "Cabal Evangel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addTappedOpponentCreature();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealAway());
        enchantment.tap();
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A stolen creature returns untapped under its owner's control")
    void stolenCreatureReturnsToOwnerUntapped() {
        Permanent creature = addTappedOpponentCreature();
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        castAndResolve(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Seal Away"));

        harness.assertNotOnBattlefield(player2, "Cabal Evangel");
        Permanent returned = findPermanent(player1, "Cabal Evangel");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    /**
     * Adds a tapped creature to an opponent's battlefield and returns it.
     */
    private Permanent addTappedOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CabalEvangel());
        creature.tap();
        return creature;
    }

    /**
     * Casts Seal Away targeting the given permanent and resolves everything.
     */
    private void castAndResolve(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // resolve enchantment spell -> Seal Away enters, ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger -> exile target
    }

    /**
     * Resets game state to allow casting more spells after castAndResolve.
     */
    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("ETB exiles target tapped opponent creature")
    void etbExilesTargetTappedCreature() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        harness.assertNotOnBattlefield(player2, "Cabal Evangel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cabal Evangel"));
    }

    @Test
    @DisplayName("Cannot target untapped creature")
    void cannotTargetUntappedCreature() {
        // Add a tapped creature so the spell is playable
        addTappedOpponentCreature();

        // Add an untapped creature
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new CabalEvangel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target own tapped creature")
    void cannotTargetOwnTappedCreature() {
        // Add a tapped opponent creature so the spell is playable
        addTappedOpponentCreature();

        // Add a tapped creature controlled by player1
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());
        ownCreature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiled creature returns when Seal Away is destroyed")
    void exiledCreatureReturnsWhenDestroyed() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cabal Evangel"));

        resetForFollowUpSpell();

        // Destroy Seal Away with InvokeTheDivine
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sealAwayId); // resolve InvokeTheDivine

        // Seal Away is gone
        harness.assertNotOnBattlefield(player1, "Seal Away");

        // Exiled creature returns to battlefield under owner's control
        harness.assertOnBattlefield(player2, "Cabal Evangel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Cabal Evangel"));
    }

    @Test
    @DisplayName("Exiled creature returns when Seal Away is bounced")
    void exiledCreatureReturnsWhenBounced() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        resetForFollowUpSpell();

        // Bounce Seal Away with BlinkOfAnEye
        harness.setHand(player2, List.of(new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sealAwayId); // resolve BlinkOfAnEye

        // Seal Away is back in hand
        harness.assertNotOnBattlefield(player1, "Seal Away");

        // Exiled creature returns to battlefield
        harness.assertOnBattlefield(player2, "Cabal Evangel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Cabal Evangel"));
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        resetForFollowUpSpell();

        // Destroy Seal Away
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sealAwayId);

        Permanent returned = findPermanent(player2, "Cabal Evangel");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returned creature under owner's control, not controller's")
    void returnedCreatureUnderOwnersControl() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        resetForFollowUpSpell();

        // Destroy Seal Away
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sealAwayId);

        // Returns under player2's control (the owner)
        harness.assertOnBattlefield(player2, "Cabal Evangel");
        // Not under player1's control
        harness.assertNotOnBattlefield(player1, "Cabal Evangel");
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after Seal Away leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        // Tracking entry should exist
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        resetForFollowUpSpell();

        // Destroy Seal Away
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sealAwayId);

        // Tracking entry should be removed
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
