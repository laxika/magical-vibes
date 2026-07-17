package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloakOfConfusionTest extends BaseCardTest {

    private Permanent addEnchantedAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent aura = new Permanent(new CloakOfConfusion());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return attacker;
    }

    private void advanceToUnblockedTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting: enchanted attacker deals no combat damage and defender discards at random")
    void acceptAssignsNoDamageAndForcesDiscard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        Permanent attacker = addEnchantedAttacker();

        advanceToUnblockedTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Defender lost one card at random and the enchanted attacker dealt no combat damage.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining: enchanted attacker deals combat damage and defender keeps their cards")
    void declineDealsDamageAndNoDiscard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addEnchantedAttacker();

        advanceToUnblockedTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        // No discard, and the 2/2 attacker dealt its combat damage to the defending player.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Blocked enchanted attacker does not trigger the discard")
    void blockedDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addEnchantedAttacker();

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The attacker was blocked, so no unblocked-attack trigger fired: no may prompt, no discard.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot enchant a creature you do not control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        // A creature you control makes the Aura playable, so casting reaches target validation.
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CloakOfConfusion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
