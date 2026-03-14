package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SphinxAmbassadorTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has SphinxAmbassadorEffect on ON_COMBAT_DAMAGE_TO_PLAYER slot")
    void hasCorrectEffect() {
        SphinxAmbassador card = new SphinxAmbassador();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(SphinxAmbassadorEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Combat damage to player initiates library search of that player's library")
    void combatDamageTriggerInitiatesLibrarySearch() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        // Put known cards in opponent's library
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock));

        resolveCombat();

        // Should trigger library search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
    }

    // ===== Full flow: creature selected, wrong name guessed =====

    @Test
    @DisplayName("Selected creature with wrong name guess can be put onto battlefield under controller's control")
    void creatureWrongNamePutOntoBattlefield() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        resolveCombat();

        // Controller selects the card from library
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Opponent should now be prompted to name a card
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);

        // Opponent names wrong card
        harness.handleColorChosen(player2, "Shock");

        // Controller gets may ability prompt
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Controller accepts
        harness.handleMayAbilityChosen(player1, true);

        // Creature should be on controller's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Card should not be in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Pending choice should be cleared
        assertThat(gd.pendingSphinxAmbassadorChoice).isNull();
    }

    // ===== Creature selected, correct name guessed =====

    @Test
    @DisplayName("Selected creature with correct name guess returns card to library")
    void creatureCorrectNameReturnsToLibrary() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        resolveCombat();

        gs.handleLibraryCardChosen(gd, player1, 0);

        // Opponent names the correct card
        harness.handleColorChosen(player2, "Grizzly Bears");

        // No may ability — card should be returned to library
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Card should be back in opponent's library (shuffled, so just check presence)
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Pending choice should be cleared
        assertThat(gd.pendingSphinxAmbassadorChoice).isNull();

        // Log should indicate conditions not met (card not revealed per rules)
        assertThat(gd.gameLog).anyMatch(log -> log.contains("conditions") && log.contains("not met"));
    }

    // ===== Non-creature selected =====

    @Test
    @DisplayName("Selected non-creature card returns to library regardless of name guess")
    void nonCreatureReturnsToLibrary() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        Card shock = new Shock();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(shock);

        resolveCombat();

        gs.handleLibraryCardChosen(gd, player1, 0);

        // Opponent names a wrong card
        harness.handleColorChosen(player2, "Grizzly Bears");

        // No may ability — non-creature card should be returned to library
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shock"));

        // Card should be back in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));

        // Pending choice should be cleared
        assertThat(gd.pendingSphinxAmbassadorChoice).isNull();

        // Log should indicate conditions not met (card not revealed per rules)
        assertThat(gd.gameLog).anyMatch(log -> log.contains("conditions") && log.contains("not met"));

        // Card name should NOT be revealed in the log (per ruling #3)
        assertThat(gd.gameLog).noneMatch(log -> log.contains("reveals") && log.contains("Grizzly Bears"));
    }

    // ===== Non-creature card name not revealed =====

    @Test
    @DisplayName("Non-creature card name is not revealed in log when conditions not met")
    void nonCreatureCardNameNotRevealed() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        Card shock = new Shock();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(shock);

        resolveCombat();

        gs.handleLibraryCardChosen(gd, player1, 0);
        harness.handleColorChosen(player2, "Grizzly Bears");

        // Card name should NOT appear in any "reveals" log entry
        assertThat(gd.gameLog).noneMatch(log -> log.contains("reveals") && log.contains("Shock"));
    }

    // ===== Controller declines to put creature =====

    @Test
    @DisplayName("Controller declining may ability returns creature to library and shuffles")
    void declineMayAbilityReturnsCreatureToLibrary() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        resolveCombat();

        gs.handleLibraryCardChosen(gd, player1, 0);
        harness.handleColorChosen(player2, "Shock"); // wrong guess
        harness.handleMayAbilityChosen(player1, false); // decline

        // Creature should NOT be on controller's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Card should be back in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Pending choice should be cleared
        assertThat(gd.pendingSphinxAmbassadorChoice).isNull();

        // Log should mention decline
        assertThat(gd.gameLog).anyMatch(log -> log.contains("declines"));
    }

    // ===== Library shuffled =====

    @Test
    @DisplayName("Opponent's library is shuffled after the interaction completes")
    void libraryIsShuffled() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        resolveCombat();

        gs.handleLibraryCardChosen(gd, player1, 0);
        harness.handleColorChosen(player2, "Shock");
        harness.handleMayAbilityChosen(player1, true);

        // Log should mention shuffling
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled") || log.contains("library is shuffled"));
    }

    // ===== Combat damage still dealt =====

    @Test
    @DisplayName("Sphinx Ambassador deals combat damage even during the search flow")
    void dealsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        resolveCombat();

        // Sphinx Ambassador is 5/5, should deal 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty opponent library skips search")
    void emptyLibrarySkipsSearch() {
        Permanent sphinx = addReadyCreature(player1, new SphinxAmbassador());
        sphinx.setAttacking(true);

        gd.playerDecks.get(player2.getId()).clear();

        resolveCombat();

        // Should not present library search for empty library
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }
}
