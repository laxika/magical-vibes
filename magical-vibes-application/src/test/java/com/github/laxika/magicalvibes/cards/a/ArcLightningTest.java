package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PouncingJaguar;
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

@CardUsed({ArcLightning.class, PouncingJaguar.class})
class ArcLightningTest extends BaseCardTest {

    @Test
    void dealsAllDamageToOneTarget() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void dividesDamageAmongTwoTargets() {
        Permanent jaguar = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(jaguar.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pouncing Jaguar");
        harness.assertLife(player2, 19);
    }

    @Test
    void dividesDamageAmongThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PouncingJaguar());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0,
                Map.of(first.getId(), 1, second.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    void assignmentsMustSumToThree() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void everyTargetMustReceivePositiveDamage() {
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(player2.getId(), 3, player1.getId(), 0))
        ).isInstanceOf(IllegalStateException.class);
    }
}
