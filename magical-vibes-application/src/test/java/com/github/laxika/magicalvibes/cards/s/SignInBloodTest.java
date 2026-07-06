package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignInBloodTest extends BaseCardTest {

    

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void resolvesAllEffectsOnOpponent() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castSignInBloodTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castSignInBloodTargeting(player1.getId());
        harness.passBothPriorities();

        // setHand sets hand to [SignInBlood], casting removes it (0), then draws 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castSignInBloodTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castSignInBloodTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sign in Blood");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new SignInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSignInBloodTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SignInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
