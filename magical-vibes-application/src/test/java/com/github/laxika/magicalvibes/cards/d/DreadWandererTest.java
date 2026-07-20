package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreadWandererTest extends BaseCardTest {

    private void setupSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void addReturnMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Enters the battlefield tapped when cast")
    void entersTappedWhenCast() {
        harness.setHand(player1, List.of(new DreadWanderer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        setupSorcerySpeed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wanderer = findPermanent(player1, "Dread Wanderer");
        assertThat(wanderer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns it to the battlefield tapped with an empty hand")
    void returnsFromGraveyardTapped() {
        DreadWanderer wanderer = new DreadWanderer();
        harness.setGraveyard(player1, List.of(wanderer));
        harness.setHand(player1, List.of());
        addReturnMana();
        setupSorcerySpeed();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(wanderer.getId()) && p.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(wanderer.getId()));
    }

    @Test
    @DisplayName("Can activate with exactly one card in hand")
    void canActivateWithOneCardInHand() {
        DreadWanderer wanderer = new DreadWanderer();
        harness.setGraveyard(player1, List.of(wanderer));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addReturnMana();
        setupSorcerySpeed();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(wanderer.getId()));
    }

    @Test
    @DisplayName("Cannot activate with two or more cards in hand")
    void cannotActivateWithTwoCardsInHand() {
        harness.setGraveyard(player1, List.of(new DreadWanderer()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addReturnMana();
        setupSorcerySpeed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1 or fewer cards in your hand");
    }

    @Test
    @DisplayName("Cannot activate at instant speed (not the active player)")
    void cannotActivateOutsideSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new DreadWanderer()));
        harness.setHand(player1, List.of());
        addReturnMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }
}
