package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinStreetMarket.class, Forest.class, Mountain.class, GrizzlyBears.class})
class TinStreetMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability discards a card and draws a card")
    void enchantedLandLoots() {
        Permanent land = enchantedLand(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

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
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TinStreetMarket()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The land's controller discards and draws even when the opponent controls the Aura")
    void opponentsLandControllerLoots() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TinStreetMarket());
        aura.setAttachedTo(land.getId());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Mountain");
    }

    @Test
    @DisplayName("A tapped land cannot pay the granted ability's tap cost")
    void tappedLandCannotLoot() {
        Permanent land = enchantedLand(player1);
        land.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discard is paid before resolution, and a land card can be discarded")
    void discardsLandAsCostBeforeDrawing() {
        Permanent land = enchantedLand(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("An activated loot ability still resolves after the Aura leaves")
    void activatedAbilitySurvivesAuraLeaving() {
        enchantedLand(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Mountain");
    }

    private Permanent enchantedLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player, new TinStreetMarket());
        aura.setAttachedTo(land.getId());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return land;
    }
}
