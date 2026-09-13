package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingLands;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GloriousAnthem.class, GrizzlyBears.class, LivingLands.class, VerduranEnchantress.class})
class VerduranEnchantressTest extends BaseCardTest {

    // ===== Trigger fires on enchantment cast =====

    @Test
    @DisplayName("Casting an enchantment spell triggers may ability prompt")
    void enchantmentCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    // ===== Accept: draws a card =====

    @Test
    @DisplayName("Accepting draws a card")
    void acceptDrawsACard() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Accepting draws a card")
    void acceptDrawsACardUpstreamReview() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new LivingLands(), "{3}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    // ===== Decline =====

    @Test
    @DisplayName("Declining may ability does not draw")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.setLibrary(player1, List.of(new Forest()));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Verduran Enchantress"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Declining may ability does not draw")
    void declineDoesNothingUpstreamReview() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.setLibrary(player1, List.of(new Forest()));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new LivingLands(), "{3}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Verduran Enchantress"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Triggered draw still resolves if Verduran Enchantress leaves before resolution")
    void triggerResolvesIfEnchantressLeavesBeforeResolution() {
        Permanent enchantress = harness.addToBattlefieldAndReturn(player1, new VerduranEnchantress());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        gd.playerBattlefields.get(player1.getId()).remove(enchantress);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Non-enchantment spell does not trigger Verduran Enchantress")
    void nonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's enchantment does not trigger =====

    @Test
    @DisplayName("Opponent casting enchantment does not trigger Verduran Enchantress")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerduranEnchantress());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Opponent casting enchantment does not trigger Verduran Enchantress")
    void opponentEnchantmentDoesNotTriggerUpstreamReview() {
        harness.addToBattlefield(player1, new VerduranEnchantress());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new LivingLands(), "{3}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Casting an enchantment puts the optional trigger on the stack before the may choice")
    void enchantmentCastStacksTriggerBeforeMayChoice() {
        harness.addToBattlefield(player1, new VerduranEnchantress());
        harness.castFromHand(player1, new LivingLands(), "{3}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }
}
