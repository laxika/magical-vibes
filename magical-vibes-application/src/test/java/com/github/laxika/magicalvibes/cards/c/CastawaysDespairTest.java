package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CastawaysDespairTest extends BaseCardTest {

    // ===== ETB tap effect =====

    @Test
    @DisplayName("Resolving Castaway's Despair taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new CastawaysDespair()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        // Creature should be tapped by the ETB effect
        assertThat(creature.isTapped()).isTrue();
        // Castaway's Despair should be attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Castaway's Despair")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Castaway's Despair on already tapped creature keeps it tapped")
    void resolvingOnAlreadyTappedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        harness.setHand(player1, List.of(new CastawaysDespair()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Enchanted creature does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent despairPerm = new Permanent(new CastawaysDespair());
        despairPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(despairPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        enchantedCreature.tap();

        Permanent freeCreature = addCreatureReady(player2, new GrizzlyBears());
        freeCreature.tap();

        Permanent despairPerm = new Permanent(new CastawaysDespair());
        despairPerm.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(despairPerm);

        advanceToNextTurn(player1);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(freeCreature.isTapped()).isFalse();
    }

    // ===== Removal restores untapping =====

    @Test
    @DisplayName("Creature can untap again after Castaway's Despair is removed")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent despairPerm = new Permanent(new CastawaysDespair());
        despairPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(despairPerm);

        // Remove Castaway's Despair
        gd.playerBattlefields.get(player1.getId()).remove(despairPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Castaway's Despair fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CastawaysDespair()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Castaway's Despair");
        harness.assertNotOnBattlefield(player1, "Castaway's Despair");
    }

    // ===== Full integration =====

    @Test
    @DisplayName("Full integration: cast Castaway's Despair, creature gets tapped, stays tapped through untap step")
    void fullIntegration() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CastawaysDespair()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        // Creature should be tapped by ETB
        assertThat(creature.isTapped()).isTrue();

        // Advance to player2's turn — creature should not untap
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
