package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidScout.class, Forest.class, Island.class})
class CephalidScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{U} and sacrificing a land draws a card")
    void sacrificesLandAndDrawsCard() {
        harness.addToBattlefield(player1, new CephalidScout());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new CephalidScout());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void requiresMana() {
        harness.addToBattlefield(player1, new CephalidScout());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The land is sacrificed as a cost before the card is drawn")
    void sacrificesLandBeforeResolution() {
        harness.addToBattlefield(player1, new CephalidScout());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Scout can activate repeatedly and sacrifice tapped lands")
    void tappedScoutCanActivateRepeatedly() {
        harness.addToBattlefield(player1, new CephalidScout());
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new Forest());
            gd.playerBattlefields.get(player1.getId()).getLast().setTapped(true);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Cephalid Scout");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new CephalidScout());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
