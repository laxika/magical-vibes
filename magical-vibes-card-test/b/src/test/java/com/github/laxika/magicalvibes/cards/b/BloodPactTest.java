package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodPact.class, DawnhartRejuvenator.class, Plains.class})
class BloodPactTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void targetPlayerDrawsAndLosesLife() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodPact()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Blood Pact");
    }

    @Test
    @DisplayName("Caster can target themselves and only they draw and lose life")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodPact()));
        harness.setHand(player2, List.of());
        Plains firstCard = new Plains();
        BloodPact secondCard = new BloodPact();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Blood Pact");
    }

    @Test
    @DisplayName("A player with only one library card still loses life before losing the game")
    void insufficientLibraryStillLosesLife() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodPact()));
        harness.setHand(player2, List.of());
        Plains remainingCard = new Plains();
        harness.setLibrary(player2, List.of(remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new BloodPact()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
