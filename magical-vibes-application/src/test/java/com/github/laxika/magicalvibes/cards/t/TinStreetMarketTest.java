package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TinStreetMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability discards a card and draws a card")
    void enchantedLandLoots() {
        Permanent land = enchantedLand(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Granted ability cannot be activated with an empty hand")
    void cannotActivateWithEmptyHand() {
        enchantedLand(player1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted ability goes away when the Aura leaves the battlefield")
    void abilityLostWhenAuraLeaves() {
        Permanent land = enchantedLand(player1);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).get(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can enchant a land an opponent controls")
    void canEnchantOpponentsLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new TinStreetMarket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, opponentForest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> assertThat(p.getAttachedTo()).isEqualTo(opponentForest.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a nonland permanent")
    void cannotEnchantCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new TinStreetMarket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private Permanent enchantedLand(Player player) {
        harness.addToBattlefield(player, new Forest());
        Permanent land = gd.playerBattlefields.get(player.getId()).getFirst();
        Permanent aura = new Permanent(new TinStreetMarket());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return land;
    }
}
