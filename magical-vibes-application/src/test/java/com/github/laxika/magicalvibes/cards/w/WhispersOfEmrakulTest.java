package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhispersOfEmrakul.class, Forest.class, GrizzlyBears.class, Pacifism.class, Shock.class})
class WhispersOfEmrakulTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards one card at random without delirium")
    void targetOpponentDiscardsOneCardWithoutDelirium() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target opponent discards two cards at random with delirium")
    void targetOpponentDiscardsTwoCardsWithDelirium() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Shock(), new Pacifism(), new Forest()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The resolving sorcery does not supply a fourth graveyard card type")
    void resolvingSpellDoesNotCountTowardsDelirium() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Pacifism(), new Forest()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Whispers of Emrakul");
    }

    @Test
    @DisplayName("Delirium uses the caster's graveyard, not the opponent's")
    void opponentDeliriumDoesNotUpgradeDiscard() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism(), new Forest()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Delirium gained in response upgrades the discard at resolution")
    void deliriumGainedInResponse() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul(), new Shock()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new Pacifism()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Pacifism(), new Forest()));
        addManaForSpell();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With delirium a one-card hand discards its only card")
    void deliriumWithOnlyOneCardInHand() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Pacifism(), new Forest()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent with an empty hand remains a legal target")
    void emptyHandWithDelirium() {
        harness.setHand(player1, List.of(new WhispersOfEmrakul()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Pacifism(), new Forest()));
        addManaForSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Whispers of Emrakul");
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
