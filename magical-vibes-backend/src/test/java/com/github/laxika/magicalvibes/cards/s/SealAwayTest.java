package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SealAwayTest extends BaseCardTest {

    /**
     * Adds a tapped creature to an opponent's battlefield and returns it.
     */
    private Permanent addTappedOpponentCreature() {
        Permanent creature = new Permanent(new GoblinPiker());
        creature.tap();
        gd.playerBattlefields.get(player2.getId()).add(creature);
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

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB exiles target tapped opponent creature")
    void etbExilesTargetTappedCreature() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target untapped creature")
    void cannotTargetUntappedCreature() {
        // Add a tapped creature so the spell is playable
        addTappedOpponentCreature();

        // Add an untapped creature
        Permanent untapped = new Permanent(new GoblinPiker());
        gd.playerBattlefields.get(player2.getId()).add(untapped);

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
        Permanent ownCreature = new Permanent(new GoblinPiker());
        ownCreature.tap();
        gd.playerBattlefields.get(player1.getId()).add(ownCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SealAway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== LTB return =====

    @Test
    @DisplayName("Exiled creature returns when Seal Away is destroyed")
    void exiledCreatureReturnsWhenDestroyed() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));

        resetForFollowUpSpell();

        // Destroy Seal Away with Naturalize
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sealAwayId);
        harness.passBothPriorities(); // resolve Naturalize

        // Seal Away is gone
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Seal Away"));

        // Exiled creature returns to battlefield under owner's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled creature returns when Seal Away is bounced")
    void exiledCreatureReturnsWhenBounced() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        resetForFollowUpSpell();

        // Bounce Seal Away with Disperse
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sealAwayId);
        harness.passBothPriorities(); // resolve Disperse

        // Seal Away is back in hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Seal Away"));

        // Exiled creature returns to battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        resetForFollowUpSpell();

        // Destroy Seal Away
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sealAwayId);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returned creature under owner's control, not controller's")
    void returnedCreatureUnderOwnersControl() {
        Permanent creature = addTappedOpponentCreature();
        castAndResolve(creature.getId());

        resetForFollowUpSpell();

        // Destroy Seal Away
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sealAwayId);
        harness.passBothPriorities();

        // Returns under player2's control (the owner)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        // Not under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Piker"));
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
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sealAwayId = harness.getPermanentId(player1, "Seal Away");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sealAwayId);
        harness.passBothPriorities();

        // Tracking entry should be removed
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
