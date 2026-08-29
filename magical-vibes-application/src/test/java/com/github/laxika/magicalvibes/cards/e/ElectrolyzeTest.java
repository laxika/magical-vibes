package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Electrolyze.class, GrizzlyBears.class})
class ElectrolyzeTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToOneTargetAndDrawsACard() {
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void dividesDamageAmongTwoTargetsAndDrawsACard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, Map.of(bears.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId())
                        && permanent.getMarkedDamage() == 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(
                first.getId(), 1,
                second.getId(), 1,
                third.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }
}
