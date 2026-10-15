package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({FiendHunter.class, GoblinPiker.class, LightningBolt.class, Unsummon.class})
class FiendHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Enter trigger still exiles its target after Fiend Hunter leaves")
    void targetIsExiledEvenIfHunterLeavesBeforeEnterTriggerResolves() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Fiend Hunter"));

        harness.assertInHand(player1, "Fiend Hunter");
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Goblin Piker");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fiend Hunter can exile another creature its controller owns")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new GoblinPiker());
        castAndExileTarget(harness.getPermanentId(player1, "Goblin Piker"));

        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its former controller")
    void stolenCreatureReturnsToOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        castAndExileTarget(stolen.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Fiend Hunter"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Piker");
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
    }

    @Test
    @DisplayName("Fiend Hunter cannot target itself when it is the only creature")
    void noEnterTriggerRemainsWithoutAnotherCreature() {
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fiend Hunter");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    /**
     * Casts Fiend Hunter, resolves it, accepts the may ability,
     * chooses a target creature, and resolves the ETB trigger.
     */
    private void castAndExileTarget(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    /**
     * Resets game state to allow casting more spells after castAndExileTarget.
     */
    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }


    @Test
    @DisplayName("Resolving triggers may ability prompt when creature exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Goblin Piker"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("ETB exiles target creature")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Goblin Piker"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Fiend Hunter");
        harness.assertOnBattlefield(player2, "Goblin Piker");
    }


    @Test
    @DisplayName("Exiled card returns when Fiend Hunter dies")
    void exiledCardReturnsWhenHunterDies() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Verify creature is exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter with Lightning Bolt
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunterId);

        // Fiend Hunter is dead
        harness.assertNotOnBattlefield(player1, "Fiend Hunter");

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Exiled card returns to battlefield under owner's control
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled card returns when Fiend Hunter is bounced")
    void exiledCardReturnsWhenHunterBounced() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Bounce Fiend Hunter with Unsummon
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunterId);

        // Fiend Hunter is back in hand
        harness.assertNotOnBattlefield(player1, "Fiend Hunter");

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Exiled card returns to battlefield
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled card returns under owner's control, not controller's")
    void exiledCardReturnsUnderOwnersControl() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunterId);

        harness.passBothPriorities();

        // Card returns under player2's control (the owner)
        harness.assertOnBattlefield(player2, "Goblin Piker");
        // Not under player1's control
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
    }


    @Test
    @DisplayName("Nothing returns if may was declined")
    void nothingReturnsIfMayDeclined() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Goblin Piker"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunterId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Goblin Piker is still on battlefield (was never exiled)
        harness.assertOnBattlefield(player2, "Goblin Piker");
        // Nothing weird returned
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunterId);

        harness.passBothPriorities();

        // The returned creature should have summoning sickness
        Permanent returned = findPermanent(player2, "Goblin Piker");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after source leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // There should be a tracking entry
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hunterId);

        // Tracking entry should be removed
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
