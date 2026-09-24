package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeteorBlast.class, GrizzlyBears.class, Plains.class})
class MeteorBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each of exactly X targets")
    void dealsFourDamageToEachTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 2, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals the full 4 damage to creature and player targets")
    void dealsFullDamageToCreatureAndPlayer() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 2, List.of(bear.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a land as an any-target")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Plains());
        Permanent plains = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new MeteorBlast()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
