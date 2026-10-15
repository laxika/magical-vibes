package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineReckoning.class, WalkingCorpse.class, TyphoidRats.class})
class DivineReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Divine Reckoning");
    }

    @Test
    @DisplayName("Players with exactly one creature auto-keep it, no prompt")
    void singleCreatureAutoKept() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new TyphoidRats());

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Both creatures survive — they were auto-kept since they were the only ones
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Typhoid Rats");
    }

    @Test
    @DisplayName("Player with multiple creatures is prompted to choose one to keep")
    void multipleCreaturesPromptsChoice() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 (active player) has 2 creatures and must choose
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.DestroyRestChoice.class);

        // Player1 chooses to keep Walking Corpse
        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Walking Corpse survives, Typhoid Rats is destroyed
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Typhoid Rats");
        harness.assertInGraveyard(player1, "Typhoid Rats");
    }

    @Test
    @DisplayName("Both players with multiple creatures are prompted sequentially (APNAP)")
    void bothPlayersPromptedSequentially() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new TyphoidRats());

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 (active player, APNAP first) is prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        // Player1 keeps Walking Corpse
        UUID p1BearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.handleMultiplePermanentsChosen(player1, List.of(p1BearsId));

        // Player2 is now prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        // Player2 keeps Typhoid Rats
        UUID p2ElvesId = harness.getPermanentId(player2, "Typhoid Rats");
        harness.handleMultiplePermanentsChosen(player2, List.of(p2ElvesId));

        // Verify results: each player kept their chosen creature
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Typhoid Rats");

        harness.assertOnBattlefield(player2, "Typhoid Rats");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Player with one creature auto-keeps, other player prompted")
    void mixedAutoKeepAndPrompt() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new WalkingCorpse()); // Only one creature

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 has 2 creatures, must choose
        PendingInteraction.MultiPermanentChoice mixedChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(mixedChoice).isNotNull();
        assertThat(mixedChoice.playerId()).isEqualTo(player1.getId());

        // Player2's single creature was auto-protected
        assertThat(((MultiPermanentChoiceContext.DestroyRestChoice) mixedChoice.context()).protectedIds()).hasSize(1);

        // Player1 keeps TyphoidRats
        UUID elvesId = harness.getPermanentId(player1, "Typhoid Rats");
        harness.handleMultiplePermanentsChosen(player1, List.of(elvesId));

        // Player1: Typhoid Rats kept, Walking Corpse destroyed
        harness.assertOnBattlefield(player1, "Typhoid Rats");
        harness.assertNotOnBattlefield(player1, "Walking Corpse");

        // Player2: Walking Corpse auto-kept
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Indestructible creature survives even when not chosen")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new TyphoidRats());

        // Grant Typhoid Rats indestructible
        elves.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 chooses to keep Walking Corpse
        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Both survive — Bears was chosen, Elves is indestructible
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Typhoid Rats");
    }

    @Test
    @DisplayName("Creature with regeneration shield survives when not chosen")
    void regenerationShieldSurvives() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new TyphoidRats());

        // Give Typhoid Rats a regeneration shield
        elves.setRegenerationShield(1);

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 chooses to keep Walking Corpse
        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Both survive — Bears was chosen, Elves regenerated
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Typhoid Rats");
    }

    @Test
    @DisplayName("Flashback from graveyard works correctly")
    void flashbackWorks() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());

        harness.setGraveyard(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Player1 must choose a creature to keep
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).context())
                .isInstanceOf(MultiPermanentChoiceContext.DestroyRestChoice.class);

        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        // Walking Corpse survives
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Typhoid Rats");
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new WalkingCorpse());

        harness.setGraveyard(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Single creature auto-resolves, no prompt needed
        harness.assertNotInGraveyard(player1, "Divine Reckoning");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divine Reckoning"));
    }

    @Test
    @DisplayName("Spell goes to graveyard after normal cast")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Divine Reckoning");
    }

    @Test
    @DisplayName("Works correctly when only one player has creatures")
    void onlyOnePlayerHasCreatures() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());
        // Player2 has no creatures

        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Player1 must choose one to keep
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID bearsId = harness.getPermanentId(player1, "Walking Corpse");
        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Typhoid Rats");
    }

    @Test
    @DisplayName("A player controlling multiple creatures must choose one to keep")
    void cannotDeclineMandatoryCreatureChoice() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.setHand(player1, List.of(new DivineReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Typhoid Rats");

        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Walking Corpse")));
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Typhoid Rats");
    }

    @Test
    @DisplayName("Player two chooses first when casting on their own turn")
    void choicesStartWithActivePlayerTwo() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new TyphoidRats());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new TyphoidRats());
        harness.setHand(player2, List.of(new DivineReckoning()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Typhoid Rats")));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.assertOnBattlefield(player2, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Typhoid Rats");

        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Walking Corpse")));
        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Typhoid Rats");
        harness.assertOnBattlefield(player2, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }
}
