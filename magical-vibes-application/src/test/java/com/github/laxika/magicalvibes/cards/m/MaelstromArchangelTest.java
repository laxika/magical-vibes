package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaelstromArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("On combat damage to a player, may cast a hand spell for free (no mana paid)")
    void castsHandSpellForFree() {
        addAttackingArchangel(player1);
        GrizzlyBears freeSpell = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(freeSpell)));
        // Deliberately add no mana — the spell must be castable without paying its cost.

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(freeSpell.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(freeSpell.getId()));
    }

    @Test
    @DisplayName("Declining leaves the card in hand and casts nothing")
    void decliningCastsNothing() {
        addAttackingArchangel(player1);
        GrizzlyBears handCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(handCard)));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(handCard.getId()));
    }

    @Test
    @DisplayName("No offer when the hand is empty")
    void noOfferWithEmptyHand() {
        addAttackingArchangel(player1);
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        Permanent archangel = addAttackingArchangel(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addAttackingArchangel(Player player) {
        Permanent archangel = addCreatureReady(player, new MaelstromArchangel());
        archangel.setAttacking(true);
        return archangel;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
