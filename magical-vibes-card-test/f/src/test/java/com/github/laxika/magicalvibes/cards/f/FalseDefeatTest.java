package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.e.EmptyCityRuse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalseDefeat.class, AlertShuInfantry.class, EmptyCityRuse.class})
class FalseDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Card creature = new AlertShuInfantry();
        Card spell = new FalseDefeat();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Alert Shu Infantry");
        harness.assertNotInGraveyard(player1, "Alert Shu Infantry");
        harness.assertInGraveyard(player1, "False Defeat");
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new EmptyCityRuse();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new FalseDefeat()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target card in opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new AlertShuInfantry();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new FalseDefeat()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Does not return the target if it leaves the graveyard before resolution")
    void doesNotReturnTargetThatLeavesGraveyard() {
        Card creature = new AlertShuInfantry();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new FalseDefeat()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alert Shu Infantry");
    }

    @Test
    @DisplayName("Returns only the selected creature when multiple creatures are in the graveyard")
    void returnsOnlySelectedCreature() {
        Card selected = new AlertShuInfantry();
        Card other = new AlertShuInfantry();
        harness.setGraveyard(player1, List.of(other, selected));
        harness.setHand(player1, List.of(new FalseDefeat()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, selected.getId());

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(selected.getId());
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(other.getId()))
                .noneMatch(card -> card.getId().equals(selected.getId()));
        harness.assertNotOnBattlefield(player2, "Alert Shu Infantry");
    }
}
