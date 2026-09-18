package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Browbeat.class, SuntailHawk.class})
class BrowbeatTest extends BaseCardTest {

    private void castAndResolveToChoice(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Browbeat()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Active player accepts and takes 5 damage")
    void activePlayerAcceptsDamage() {
        castAndResolveToChoice(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("If every player declines, the target player draws three cards")
    void everyoneDeclinesTargetDrawsThree() {
        castAndResolveToChoice(player2.getId());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The first player to accept takes the damage and stops the choices")
    void opponentAcceptsAfterActivePlayerDeclines() {
        castAndResolveToChoice(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The caster can be the target of the fallback draw")
    void casterDrawsWhenEveryoneDeclines() {
        castAndResolveToChoice(player1.getId());
        int casterHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(casterHandBefore + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("Browbeat cannot target a creature")
    void cannotTargetCreature() {
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Browbeat()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
