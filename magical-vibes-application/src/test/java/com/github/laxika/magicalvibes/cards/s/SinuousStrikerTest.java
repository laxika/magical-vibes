package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SinuousStrikerTest extends BaseCardTest {

    // ===== {U}: +1/-1 self-boost =====

    @Test
    @DisplayName("{U} gives this creature +1/-1 until end of turn")
    void selfBoostPlusOneMinusOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new SinuousStriker());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Base 2/2 → 3/1.
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, striker)).isEqualTo(1);
    }

    @Test
    @DisplayName("The +1/-1 boost wears off at end of turn")
    void selfBoostWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new SinuousStriker());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, striker)).isEqualTo(2);
    }

    // ===== Eternalize—{3}{U}{U}, Discard a card =====

    private void setUpEternalize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new SinuousStriker()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent eternalizedToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sinuous Striker") && p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Eternalize discards a card, exiles the source, and creates a 4/4 black Zombie Snake Warrior token")
    void eternalizeCreatesFourFourBlackZombieToken() {
        setUpEternalize();

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0); // pay the discard cost
        harness.passBothPriorities(); // resolve Eternalize → token enters

        // Discard cost: the hand card went to the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Exile cost: the source card left the graveyard for exile.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Sinuous Striker"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sinuous Striker"));

        Permanent token = eternalizedToken();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.SNAKE, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Eternalize cannot be activated with an empty hand (no card to discard)")
    void eternalizeCannotBeActivatedWithEmptyHand() {
        setUpEternalize();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sinuous Striker"));
    }

    @Test
    @DisplayName("Eternalize can only be activated at sorcery speed")
    void eternalizeOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new SinuousStriker()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sinuous Striker"));
    }
}
