package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverHoopoe.class})
class RiverHoopoeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3}{G}{U} gains 2 life and draws a card")
    void activateGainsLifeAndDraws() {
        Permanent hoopoe = harness.addToBattlefieldAndReturn(player1, new RiverHoopoe());
        harness.setLibrary(player1, List.of(new RiverHoopoe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoopoe);

        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "River Hoopoe");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent hoopoe = harness.addToBattlefieldAndReturn(player1, new RiverHoopoe());
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoopoe);

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hoopoe can activate repeatedly")
    void tappedHoopoeCanActivateRepeatedly() {
        Permanent hoopoe = harness.addToBattlefieldAndReturn(player1, new RiverHoopoe());
        hoopoe.setTapped(true);
        hoopoe.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RiverHoopoe(), new RiverHoopoe()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoopoe);

        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(hoopoe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability resolves for its controller after Hoopoe leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent hoopoe = harness.addToBattlefieldAndReturn(player2, new RiverHoopoe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new RiverHoopoe()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(hoopoe);

        harness.activateAbility(player2, idx, null, null);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        gd.playerBattlefields.get(player2.getId()).remove(hoopoe);
        gd.playerGraveyards.get(player2.getId()).add(hoopoe.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertLife(player1, 20);
        harness.assertInHand(player2, "River Hoopoe");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
