package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptidePilferer.class, GrizzlyBears.class})
class RiptidePilfererTest extends BaseCardTest {

    @Test
    void combatDamageMakesDamagedPlayerDiscard() {
        Permanent pilferer = addAttackingPilferer(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void blockedCombatDamageDoesNotTriggerDiscard() {
        Permanent pilferer = addAttackingPilferer(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlue() {
        harness.setHand(player1, List.of(new RiptidePilferer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent pilferer = findPermanent(player1, "Riptide Pilferer");
        assertThat(pilferer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pilferer));
        harness.passBothPriorities();

        assertThat(pilferer.isFaceDown()).isFalse();
    }

    private Permanent addAttackingPilferer(Player player) {
        Permanent pilferer = addCreatureReady(player, new RiptidePilferer());
        pilferer.setAttacking(true);
        return pilferer;
    }
}
