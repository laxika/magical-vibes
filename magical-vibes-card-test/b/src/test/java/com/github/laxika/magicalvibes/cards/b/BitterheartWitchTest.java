package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.c.CurseOfThePiercedHeart;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterheartWitch.class, WalkingCorpse.class, CurseOfThePiercedHeart.class, WrathOfGod.class})
class BitterheartWitchTest extends BaseCardTest {

    @Test
    @DisplayName("When Bitterheart Witch dies, controller is prompted to choose a target player")
    void deathTriggerPromptsForTargetPlayer() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Witch should be in graveyard
        harness.assertInGraveyard(player1, "Bitterheart Witch");

        // Player1 should be prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger searches library for Curse card and puts it onto battlefield attached to target player")
    void deathTriggerSearchesForCurseAndAttaches() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Card curse = new CurseOfThePiercedHeart();
        setupLibraryWithCurse(player1, curse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player (player2)
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bitterheart Witch");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        // Resolve the triggered ability — "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        // Choose the curse card (index 0)
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        // Curse should be on the battlefield under controller's control
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);

        // Curse should be attached to target player
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(cursePerm.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Death trigger can target self (attach Curse to own player)")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Card curse = new CurseOfThePiercedHeart();
        setupLibraryWithCurse(player1, curse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities(); // resolve triggered ability → "you may search" prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        // Curse should be attached to player1
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(cursePerm.getAttachedTo()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger with no Curse in library — search finds nothing, library is shuffled")
    void deathTriggerNoCurseInLibrary() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        // Library with no curse cards
        harness.setLibrary(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability — "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // No curse cards in library — no library search opened
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no Curse cards"));
    }

    @Test
    @DisplayName("Death trigger with fail to find — no Curse enters battlefield")
    void deathTriggerFailToFind() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Card curse = new CurseOfThePiercedHeart();
        setupLibraryWithCurse(player1, curse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability → "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Fail to find (index -1)
        harness.handleCardChosen(player1, -1);

        // No new permanent on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    }

    @Test
    @DisplayName("Death trigger declined — player chooses not to search, no Curse enters battlefield")
    void deathTriggerDeclinedMay() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Card curse = new CurseOfThePiercedHeart();
        setupLibraryWithCurse(player1, curse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability → "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline the search
        harness.handleMayAbilityChosen(player1, false);

        // No library search opened, no new permanent on the battlefield
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    }

    @Test
    @DisplayName("Curse attached to player is not removed as orphaned aura")
    void curseAttachedToPlayerNotOrphaned() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Card curse = new CurseOfThePiercedHeart();
        setupLibraryWithCurse(player1, curse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve → "you may search" prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        // Verify curse is on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE));

        // Advance through several steps to trigger SBA / orphan aura checks
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Curse should still be on the battlefield (not removed as orphaned)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE));
    }

    @Test
    @DisplayName("Bitterheart Witch dies from Wrath of God — death trigger still fires")
    void diesFromWrathOfGod() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new WalkingCorpse());

        Card curse = new CurseOfThePiercedHeart();
        setupLibraryWithCurse(player1, curse);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // resolve Wrath — all creatures die

        // Witch should be in graveyard
        harness.assertInGraveyard(player1, "Bitterheart Witch");

        // Player1 should be prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // resolve triggered ability → "you may search" prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        // Curse should be on the battlefield attached to player2
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(cursePerm.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Deathtouch kills the blocker even though the Witch only deals one damage")
    void deathtouchKillsBlocker() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        setupCombatWhereWitchDies();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bitterheart Witch");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("The death trigger searches its controller's library, not the targeted player's library")
    void searchesControllersLibraryOnly() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.setLibrary(player1, List.of(new WalkingCorpse()));
        CurseOfThePiercedHeart opponentsCurse = new CurseOfThePiercedHeart();
        harness.setLibrary(player2, List.of(opponentsCurse));
        setupCombatWhereWitchDies();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCurse);
        harness.assertNotOnBattlefield(player1, "Curse of the Pierced Heart");
        harness.assertNotOnBattlefield(player2, "Curse of the Pierced Heart");
    }

    @Test
    @DisplayName("Searching an empty library finishes the death trigger without a card choice")
    void emptyLibraryFinishesTrigger() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.setLibrary(player1, List.of());
        setupCombatWhereWitchDies();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Curse of the Pierced Heart");
    }

    private void setupCombatWhereWitchDies() {
        Permanent witchPerm = findPermanent(player1, "Bitterheart Witch");
        witchPerm.setSummoningSick(false);
        witchPerm.setAttacking(true);

        // Witch is 1/2, needs to be blocked by something that kills it
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void setupLibraryWithCurse(com.github.laxika.magicalvibes.model.Player player, Card curseCard) {
        harness.setLibrary(player, List.of(curseCard, new WalkingCorpse()));
    }
}
