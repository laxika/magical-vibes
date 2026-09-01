package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GroveOfTheBurnwillows.class})
class GroveOfTheBurnwillowsTest extends BaseCardTest {

    @Test
    void tappingForColorlessManaDoesNotGiveLife() {
        harness.addToBattlefield(player1, new GroveOfTheBurnwillows());
        GameData gd = harness.getGameData();
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent grove = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void tappingForRedManaGivesOpponentLife() {
        harness.addToBattlefield(player1, new GroveOfTheBurnwillows());
        GameData gd = harness.getGameData();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore + 1);
    }

    @Test
    void tappingForGreenManaGivesOpponentLife() {
        harness.addToBattlefield(player1, new GroveOfTheBurnwillows());
        GameData gd = harness.getGameData();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore + 1);
    }
}
