package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrandsOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature from your graveyard, paying 2 life and sacrificing a Swamp")
    void returnsCreatureToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
        // Swamp sacrificed, 2 life paid.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate without a Swamp to sacrifice")
    void cannotActivateWithoutSwamp() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in your graveyard")
    void cannotTargetNonCreature() {
        Card instant = new HolyDay();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new StrandsOfNight());
        harness.addToBattlefield(player1, new Swamp());
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}
