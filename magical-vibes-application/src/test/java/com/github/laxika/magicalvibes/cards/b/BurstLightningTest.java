package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurstLightningTest extends BaseCardTest {

    @Test
    void deals2DamageToAnyTargetWithoutKicker() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        Permanent damagedGiant = gameData.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(giant.getId()))
                .findFirst().orElseThrow();
        assertThat(damagedGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void deals4DamageToAnyTargetWhenKicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castKickedInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(giant.getId()));
    }

    @Test
    void dealsDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void cannotTargetLand() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent land = addToBattlefield(player2, new Plains());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
