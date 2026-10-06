package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaltRoadPackbeast.class, Forest.class})
class SaltRoadPackbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for creatures reduces the generic mana cost")
    void affinityForCreaturesReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SaltRoadPackbeast());
        }
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only creatures controlled by the spell's controller")
    void affinityCountsOnlyControlledCreatures() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new SaltRoadPackbeast());
        }
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("When Salt Road Packbeast enters, its controller draws a card")
    void etbDrawsCard() {
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    void affinityCountsTappedCreaturesAndLeavesRemainingGenericCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new SaltRoadPackbeast()).setTapped(true);
        }
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void affinityDoesNotCountNoncreaturePermanents() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void excessAffinityCannotPayWhiteManaRequirement() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new SaltRoadPackbeast());
        }
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void excessAffinityStillAllowsCastingForOneWhiteMana() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new SaltRoadPackbeast());
        }
        harness.setHand(player1, List.of(new SaltRoadPackbeast()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void enteringWithoutCastingDrawsForTheEnteringCreaturesController() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int otherPlayersHandSize = gd.playerHands.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player2, new SaltRoadPackbeast());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(otherPlayersHandSize);
    }
}
