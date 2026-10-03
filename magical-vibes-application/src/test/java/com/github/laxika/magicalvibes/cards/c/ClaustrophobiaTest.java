package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.v.VillageBellRinger;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Claustrophobia.class, DarkthicketWolf.class, VillageBellRinger.class})
class ClaustrophobiaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Claustrophobia taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        // Creature should be tapped by the ETB effect
        assertThat(creature.isTapped()).isTrue();
        // Claustrophobia should be attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Claustrophobia")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Resolving Claustrophobia on already tapped creature keeps it tapped")
    void resolvingOnAlreadyTappedCreature() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());
        creature.tap();

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());
        creature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchantedCreature = addCreatureReady(player2, new DarkthicketWolf());
        enchantedCreature.tap();

        Permanent freeCreature = addCreatureReady(player2, new DarkthicketWolf());
        freeCreature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        advanceToNextTurn(player1);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(freeCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature stays tapped across multiple turns")
    void creatureStaysTappedAcrossMultipleTurns() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());
        creature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature can untap again after Claustrophobia is removed")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());
        creature.tap();

        Permanent claustrophobiaPerm = new Permanent(new Claustrophobia());
        claustrophobiaPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(claustrophobiaPerm);

        // Remove Claustrophobia
        gd.playerBattlefields.get(player1.getId()).remove(claustrophobiaPerm);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Claustrophobia fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Claustrophobia");
        harness.assertNotOnBattlefield(player1, "Claustrophobia");
    }

    @Test
    @DisplayName("Full integration: cast Claustrophobia, creature gets tapped, stays tapped through untap step")
    void fullIntegration() {
        Permanent creature = addCreatureReady(player2, new DarkthicketWolf());

        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        // Creature should be tapped by ETB
        assertThat(creature.isTapped()).isTrue();

        // Advance to player2's turn — creature should not untap
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entry trigger taps the current enchanted creature after the Aura moves")
    void entryTriggerFollowsMovedAura() {
        Permanent originalCreature = addCreatureReady(player2, new DarkthicketWolf());
        Permanent newCreature = addCreatureReady(player2, new DarkthicketWolf());
        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, originalCreature.getId());
        harness.passBothPriorities();

        assertThat(originalCreature.isTapped()).isFalse();
        assertThat(newCreature.isTapped()).isFalse();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof Claustrophobia).findFirst().orElseThrow();
        // Model an Aura-moving effect resolving in response to the entry trigger.
        aura.setAttachedTo(newCreature.getId());
        harness.passBothPriorities();

        assertThat(originalCreature.isTapped()).isFalse();
        assertThat(newCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap restriction follows the Aura when it moves")
    void untapRestrictionFollowsMovedAura() {
        Permanent originalCreature = addCreatureReady(player2, new DarkthicketWolf());
        Permanent newCreature = addCreatureReady(player2, new DarkthicketWolf());
        originalCreature.tap();
        newCreature.tap();
        Permanent aura = new Permanent(new Claustrophobia());
        aura.setAttachedTo(originalCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        aura.setAttachedTo(newCreature.getId());

        harness.performUntapStep(player2);

        assertThat(originalCreature.isTapped()).isFalse();
        assertThat(newCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another effect can untap the enchanted creature without removing the Aura")
    void anotherEffectCanUntapEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new Claustrophobia(), new VillageBellRinger()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Claustrophobia");
        creature.tap();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextPlayer = currentActivePlayer.equals(player1) ? player2 : player1;
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
