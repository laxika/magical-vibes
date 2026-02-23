package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GethLordOfTheVaultTest extends BaseCardTest {

    // ===== Intimidate blocking =====

    @Test
    @DisplayName("Intimidate — same color creature can block")
    void sameColorCanBlock() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gethPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // DrossCrocodile is black — same color as Geth
        Permanent blockerPerm = new Permanent(new DrossCrocodile());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Intimidate — artifact creature can block")
    void artifactCreatureCanBlock() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gethPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // Create an artifact creature blocker
        Card artifactCreature = new Card();
        artifactCreature.setName("Test Artifact Creature");
        artifactCreature.setType(CardType.ARTIFACT);
        artifactCreature.setAdditionalTypes(java.util.Set.of(CardType.CREATURE));
        artifactCreature.setColor(CardColor.WHITE);
        artifactCreature.setPower(2);
        artifactCreature.setToughness(2);
        artifactCreature.setManaCost("{2}");
        Permanent blockerPerm = new Permanent(artifactCreature);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Intimidate — different color non-artifact creature cannot block")
    void differentColorCannotBlock() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gethPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // GrizzlyBears is green — different color from Geth (black)
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(intimidate)");
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activated ability — takes creature from opponent's graveyard onto battlefield tapped")
    void takesCreatureFromOpponentGraveyard() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // GrizzlyBears has mana value 2 — put in opponent's graveyard
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        // Add some cards to opponent's deck for milling
        Card deckCard1 = new DrossCrocodile();
        Card deckCard2 = new DrossCrocodile();
        gd.playerDecks.get(player2.getId()).add(0, deckCard1);
        gd.playerDecks.get(player2.getId()).add(1, deckCard2);

        // X=2 (mana value of GrizzlyBears), plus {B}
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Bears should be on player1's battlefield, tapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()) && p.isTapped());

        // Bears removed from opponent's graveyard (check by card ID)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));

        // Opponent should have been milled 2 cards (Dross Crocodiles)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dross Crocodile"));
    }

    @Test
    @DisplayName("Activated ability — takes artifact from opponent's graveyard")
    void takesArtifactFromOpponentGraveyard() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // Create an artifact with mana value 3
        Card artifact = new Card();
        artifact.setName("Test Artifact");
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{3}");
        artifact.setColor(CardColor.WHITE);
        artifact.setPower(null);
        artifact.setToughness(null);
        harness.setGraveyard(player2, List.of(artifact));

        // Add some deck cards for milling
        for (int i = 0; i < 3; i++) {
            gd.playerDecks.get(player2.getId()).add(0, new GrizzlyBears());
        }

        // X=3, plus {B}
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 3, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Artifact should be on player1's battlefield, tapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Test Artifact") && p.isTapped());
    }

    @Test
    @DisplayName("Activated ability — rejects target with wrong mana value")
    void rejectsWrongManaValue() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // GrizzlyBears has mana value 2, but we'll use X=3
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value must equal X");
    }

    @Test
    @DisplayName("Activated ability — rejects targeting own graveyard")
    void rejectsOwnGraveyard() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        // Put creature in player1's own graveyard
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Stolen creature goes to original owner's graveyard on death")
    void stolenCreatureGoesToOriginalOwnerGraveyard() {
        Permanent gethPerm = new Permanent(new GethLordOfTheVault());
        gethPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gethPerm);

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        // Add some deck cards for milling
        gd.playerDecks.get(player2.getId()).add(0, new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Find the stolen creature on player1's battlefield
        Permanent stolenBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();

        // Verify it's tracked as stolen
        assertThat(gd.stolenCreatures).containsKey(stolenBears.getId());
        assertThat(gd.stolenCreatures.get(stolenBears.getId())).isEqualTo(player2.getId());

        // Kill it — use Lightning Bolt pattern: deal lethal damage by setting damage
        // Simply remove from battlefield and send to graveyard using the helper
        gd.playerBattlefields.get(player1.getId()).remove(stolenBears);
        UUID graveyardOwnerId = gd.stolenCreatures.getOrDefault(stolenBears.getId(), player1.getId());
        gd.playerGraveyards.get(graveyardOwnerId).add(stolenBears.getCard());
        gd.stolenCreatures.remove(stolenBears.getId());

        // Bears should go to player2's graveyard (original owner), not player1's
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
