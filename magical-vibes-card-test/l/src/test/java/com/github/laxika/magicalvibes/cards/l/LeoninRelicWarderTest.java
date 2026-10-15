package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({LeoninRelicWarder.class, LeoninScimitar.class, GloriousAnthem.class, Shock.class, Unsummon.class, Ornithopter.class})
class LeoninRelicWarderTest extends BaseCardTest {

    /**
     * Casts Leonin Relic-Warder, resolves it, chooses a target,
     * resolves the ETB trigger, and accepts the may ability.
     */
    private void castAndExileTarget(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

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
    @DisplayName("Resolving triggers may ability prompt when artifact exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("ETB exiles target artifact")
    void etbExilesTargetArtifact() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        castAndExileTarget(artifactId);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("ETB exiles target enchantment")
    void etbExilesTargetEnchantment() {
        UUID anthemId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        castAndExileTarget(anthemId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Leonin Relic-Warder");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Casts with no artifacts or enchantments and the ETB is removed from the stack")
    void castsWithNoLegalTargets() {
        // With no legal target, the triggered ability is removed from the stack.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters; ETB finds no legal target

        harness.assertOnBattlefield(player1, "Leonin Relic-Warder");
        // No may prompt and nothing left on the stack — the targeted trigger cannot remain on the stack.
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Exiled card returns when Leonin Relic-Warder dies")
    void exiledCardReturnsWhenWarderDies() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        castAndExileTarget(artifactId);

        // Verify artifact is exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder with Shock
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // Leonin Relic-Warder is dead
        harness.assertNotOnBattlefield(player1, "Leonin Relic-Warder");

        // Exiled card returns to battlefield under owner's control
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Exiled card returns when Leonin Relic-Warder is bounced")
    void exiledCardReturnsWhenWarderBounced() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        castAndExileTarget(artifactId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Bounce Leonin Relic-Warder with Unsummon
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // Leonin Relic-Warder is back in hand
        harness.assertNotOnBattlefield(player1, "Leonin Relic-Warder");

        // Exiled card returns to battlefield
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Exiled card returns under owner's control, not controller's")
    void exiledCardReturnsUnderOwnersControl() {
        Permanent stolenArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        gd.stolenCreatures.put(stolenArtifact.getId(), player2.getId());
        UUID artifactId = stolenArtifact.getId();
        castAndExileTarget(artifactId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // Card returns under player2's control (the owner)
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        // Not under player1's control
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Nothing returns if may was declined")
    void nothingReturnsIfMayDeclined() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // Leonin Scimitar is still on battlefield (was never exiled)
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        // Nothing weird returned
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifactId);

        // Remove target before the triggered ability resolves
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // No exile-return tracking should exist since target was gone
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        // Nothing was exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        castAndExileTarget(artifactId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // The returned permanent should have summoning sickness
        Permanent returned = findPermanent(player2, "Ornithopter");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after source leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        castAndExileTarget(artifactId);

        // There should be a tracking entry
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // Tracking entry should be removed
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Enter trigger still exiles after the Warder leaves first")
    void enterTriggerExilesAfterWarderLeaves() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);
        harness.assertInHand(player1, "Leonin Relic-Warder");

        harness.passBothPriorities(); // resolve the empty leave trigger
        harness.passBothPriorities(); // resolve the enter trigger
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiled card stays in exile until the leave trigger resolves")
    void returnUsesTheStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        castAndExileTarget(targetId);
        resetForFollowUpSpell();
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warderId);

        harness.assertInHand(player1, "Leonin Relic-Warder");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing abilities before leaving prevents the return trigger")
    void noReturnIfWarderLostAbilities() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar()).getId();
        castAndExileTarget(targetId);
        resetForFollowUpSpell();
        Permanent warder = findPermanent(player1, "Leonin Relic-Warder");
        warder.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warder.getId());

        harness.assertInHand(player1, "Leonin Relic-Warder");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        assertThat(gd.stack).isEmpty();
    }
}
