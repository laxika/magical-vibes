package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JungleHollow.class})
class JungleHollowTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield tapped gains 1 life")
    void entersTappedAndGainsOneLife() {
        harness.setHand(player1, List.of(new JungleHollow()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent hollow = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hollow.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        Permanent hollow = addHollowReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(hollow.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent hollow = addHollowReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        GameData gameData = harness.getGameData();

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(hollow.isTapped()).isTrue();
    }

    private Permanent addHollowReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JungleHollow());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Life gain waits for the entry trigger to resolve and survives removal of the land")
    void lifeGainTriggerSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new JungleHollow()));
        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent hollow = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(hollow.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry outside a land play gains life only for the entering land's controller")
    void enteringUnderOtherPlayersControlGainsLifeForThatPlayer() {
        Permanent hollow = harness.enterBattlefieldAndReturn(player2, new JungleHollow());

        assertThat(hollow.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }
}
