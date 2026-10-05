package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("scryfall")
@ExtendWith(CardUsedExtension.class)
@CardUsed({LeylineOfAnticipation.class, RuneclawBear.class, LavaAxe.class})
class LeylineOfAnticipationTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gd.alwaysOfferPriorityWindows = true;
        // Set opening hands before ending the mulligan process.
    }

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Leyline of Anticipation");
        harness.assertNotInHand(player1, "Leyline of Anticipation");
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Leyline of Anticipation");
        harness.assertInHand(player1, "Leyline of Anticipation");
    }

    @Test
    @DisplayName("Declined leyline is not re-prompted during the first upkeep")
    void declinedLeylineNotRePromptedDuringUpkeep() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        // Decline the leyline; the card stays in hand.
        harness.handleMayAbilityChosen(player1, false);

        // Game should be running now without re-prompting
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInHand(player1, "Leyline of Anticipation");
    }

    @Test
    @DisplayName("Leyline placement is logged")
    void leylinePlacementIsLogged() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("begins the game with Leyline of Anticipation"));
    }

    @Test
    @DisplayName("Multiple leylines in opening hand each prompt separately")
    void multipleLeylinesPromptSeparately() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation(), new LeylineOfAnticipation()));
        harness.skipMulligan();

        // Accept first leyline
        harness.handleMayAbilityChosen(player1, true);
        // Accept second leyline
        harness.handleMayAbilityChosen(player1, true);

        long count = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leyline of Anticipation"))
                .count();
        assertThat(count).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both players can start with leylines on the battlefield")
    void bothPlayersCanHaveLeylines() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.setHand(player2, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        // The starting player makes pregame choices first.
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Leyline of Anticipation");
        harness.assertOnBattlefield(player2, "Leyline of Anticipation");
    }

    @Test
    @DisplayName("Can cast creature at instant speed with Leyline of Anticipation on battlefield")
    void canCastCreatureAtInstantSpeed() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Runeclaw Bear");
    }

    @Test
    @DisplayName("Can cast creature during opponent's turn with Leyline of Anticipation on battlefield")
    void canCastCreatureDuringOpponentsTurn() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Player2 passes priority, giving player1 priority
        harness.passPriority(player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Runeclaw Bear");
    }

    @Test
    @DisplayName("Can cast sorcery at instant speed with Leyline of Anticipation on battlefield")
    void canCastSorceryAtInstantSpeed() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lava Axe");
    }

    @Test
    @DisplayName("Leyline of Anticipation only grants flash to its controller's spells")
    void onlyAffectsController() {
        harness.skipMulligan();
        harness.addToBattlefield(player2, new LeylineOfAnticipation());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells lose flash timing when Leyline of Anticipation leaves the battlefield")
    void spellsLoseFlashWhenLeylineLeaves() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        // Remove Leyline from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Leyline of Anticipation can be cast normally for {2}{U}{U}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Leyline of Anticipation");
    }

    @Test
    @DisplayName("Each opening-hand Leyline can be accepted or declined independently")
    void canAcceptOneLeylineAndDeclineAnother() {
        LeylineOfAnticipation accepted = new LeylineOfAnticipation();
        LeylineOfAnticipation declined = new LeylineOfAnticipation();
        harness.setHand(player1, List.of(accepted, declined));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).containsExactly(accepted.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getId()).containsExactly(declined.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A Leyline in hand does not grant flash to itself or other spells")
    void leylineInHandDoesNotGrantFlash() {
        harness.skipMulligan();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LeylineOfAnticipation(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leyline allows casting an enchantment in response to another spell")
    void canCastEnchantmentWithNonemptyStack() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RuneclawBear(), new LeylineOfAnticipation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isInstanceOf(LeylineOfAnticipation.class);
    }
}
