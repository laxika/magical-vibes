package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameStatus;
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

@CardUsed({SignInBlood.class, RuneclawBear.class, Cancel.class, AlmsCollector.class})
class SignInBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void resolvesAllEffectsOnOpponent() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castAndResolveSignInBloodTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castAndResolveSignInBloodTargeting(player1.getId());

        // setHand sets hand to [SignInBlood], casting removes it (0), then draws 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castAndResolveSignInBloodTargeting(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castAndResolveSignInBloodTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sign in Blood");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new SignInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Countering the spell prevents both drawing and life loss")
    void counteredSpellHasNoEffects() {
        harness.setHand(player1, List.of(new SignInBlood()));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spellId);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sign in Blood");
    }

    @Test
    @DisplayName("Target draws both cards before lethal life loss")
    void drawsBeforeLethalLifeLoss() {
        harness.setLife(player2, 2);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        castAndResolveSignInBloodTargeting(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed({SignInBlood.class, AlmsCollector.class})
    @DisplayName("Alms Collector replaces the two-card draw but not the life loss")
    void almsCollectorReplacesDrawInstruction() {
        harness.addToBattlefield(player1, new AlmsCollector());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castAndResolveSignInBloodTargeting(player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private void castAndResolveSignInBloodTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SignInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
