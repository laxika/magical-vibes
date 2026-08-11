package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshlingsCommandTest extends BaseCardTest {

    @Test
    void copyElementalAndDamageTargetPlayersCreatures() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent enemyElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshlingsCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(elemental.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bearsId));
        assertThat(enemyElemental.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void drawAndCreateTreasuresCanShareTargetPlayer() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new AshlingsCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(2)
                .allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copyModeRejectsElementalControlledByOpponent() {
        Permanent opponentElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new AshlingsCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 3},
                List.of(opponentElemental.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
