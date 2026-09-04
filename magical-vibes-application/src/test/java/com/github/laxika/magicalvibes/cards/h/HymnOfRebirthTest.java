package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HymnOfRebirth.class, BalduvianBears.class})
class HymnOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creature from own graveyard to the battlefield")
    void returnsCreatureFromOwnGraveyard() {
        BalduvianBears creature = new BalduvianBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HymnOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Returns creature from an opponent's graveyard under your control")
    void returnsCreatureFromOpponentGraveyard() {
        BalduvianBears creature = new BalduvianBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new HymnOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        BalduvianBears creature = new BalduvianBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HymnOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast without a creature card as a graveyard target")
    void cannotCastWithoutCreatureTarget() {
        HymnOfRebirth nonCreature = new HymnOfRebirth();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new HymnOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the targeted creature leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        BalduvianBears creature = new BalduvianBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HymnOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("fizzles"));
    }
}
