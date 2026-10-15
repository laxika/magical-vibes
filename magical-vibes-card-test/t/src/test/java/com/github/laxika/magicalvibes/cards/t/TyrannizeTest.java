package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tyrannize.class, GrizzlyBears.class, Peek.class, EverybodyLives.class})
class TyrannizeTest extends BaseCardTest {

    private void castTyrannizeOn(int targetLife) {
        harness.setLife(player2, targetLife);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek()));

        harness.setHand(player1, List.of(new Tyrannize()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Target player pays 7 life to keep their hand")
    void paysLifeKeepsHand() {
        castTyrannizeOn(20);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target player declines and discards their whole hand")
    void declinesDiscardsHand() {
        castTyrannizeOn(20);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("A target with too little life can't pay and discards automatically")
    void cannotPayDiscardsAutomatically() {
        castTyrannizeOn(5);

        // No choice offered — the target can't pay 7 life, so the hand is discarded outright.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 5);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A target with exactly 7 life can pay all of it to keep their hand")
    void exactlySevenLifeCanPay() {
        castTyrannizeOn(7);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertLife(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster can target themself and discard the remaining hand")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Tyrannize(), new Tyrannize()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An empty hand does not prevent choosing to pay life")
    void emptyHandCanStillPayLife() {
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Tyrannize()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player who cannot lose life must discard rather than pay life")
    void cannotLoseLifeMustDiscard() {
        harness.setHand(player1, List.of(new EverybodyLives(), new Tyrannize(), new Tyrannize()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
