package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LosDiablosMissileBase.class})
class LosDiablosMissileBaseTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LosDiablosMissileBase()));

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds red mana when red is chosen")
    void manaAbilityAddsRedMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds green mana when green is chosen")
    void manaAbilityAddsGreenMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain waits for its trigger and survives the land leaving")
    void lifeGainResolvesAfterLandLeaves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LosDiablosMissileBase()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering under the opponent's control gains life for that player")
    void enteringGainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent land = harness.enterBattlefieldAndReturn(player2, new LosDiablosMissileBase());

        assertThat(land.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    private Permanent addReadyLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new LosDiablosMissileBase());
        land.setSummoningSick(false);
        return land;
    }
}
