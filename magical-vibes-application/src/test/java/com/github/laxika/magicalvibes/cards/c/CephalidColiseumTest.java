package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidColiseum.class, CephalidLooter.class, Island.class})
class CephalidColiseumTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for blue mana adds {U} and deals 1 damage to its controller")
    void tapForBlueMana() {
        addReadyColiseum(player1);
        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Threshold ability sacrifices the land, then makes the target draw three and discard three")
    void thresholdAbilityDrawsAndDiscards() {
        addReadyColiseum(player1);
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setHand(player2, List.of(new CephalidLooter()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.assertInGraveyard(player1, "Cephalid Coliseum");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Threshold ability can target its controller")
    void thresholdAbilityCanTargetController() {
        addReadyColiseum(player1);
        harness.setGraveyard(player1, graveyardCards(7));
        harness.setHand(player1, List.of(new CephalidLooter()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.activePlayerId = player2.getId();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Threshold ability cannot target a non-player permanent")
    void thresholdAbilityRejectsNonPlayerTarget() {
        addReadyColiseum(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the threshold ability with fewer than seven cards in the graveyard")
    void cannotActivateWithoutThreshold() {
        addReadyColiseum(player1);
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    private Permanent addReadyColiseum(Player player) {
        return addCreatureReady(player, new CephalidColiseum());
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new CephalidLooter());
        }
        return cards;
    }
}
