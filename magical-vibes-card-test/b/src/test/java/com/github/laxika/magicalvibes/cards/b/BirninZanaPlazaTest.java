package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BirninZanaPlaza.class)
class BirninZanaPlazaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new BirninZanaPlaza()));

        harness.playLand(player1, 0);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent plaza = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(plaza.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent plaza = addPlazaReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(plaza.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        Permanent plaza = addPlazaReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(plaza.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being played gains life even after the land leaves")
    void entryTriggerResolvesAfterSourceLeaves() {
        Permanent plaza = harness.enterBattlefieldAndReturn(player1, new BirninZanaPlaza());

        assertThat(plaza.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(plaza);
        gd.playerGraveyards.get(player1.getId()).add(plaza.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPlazaReady() {
        return harness.addToBattlefieldAndReturn(player1, new BirninZanaPlaza());
    }
}
