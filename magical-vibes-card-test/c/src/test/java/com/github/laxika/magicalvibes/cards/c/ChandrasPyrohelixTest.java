package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LongtuskCub;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasPyrohelix.class, LongtuskCub.class})
class ChandrasPyrohelixTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void dividesDamageAmongTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent cub = harness.addToBattlefieldAndReturn(player2, new LongtuskCub());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, Map.of(cub.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(cub.getId())
                        && permanent.getMarkedDamage() == 1);
    }

    @Test
    void dealsLethalDamageToOneCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent cub = harness.addToBattlefieldAndReturn(player2, new LongtuskCub());

        harness.castInstant(player1, 0, Map.of(cub.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Longtusk Cub");
        harness.assertInGraveyard(player2, "Longtusk Cub");
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotAssignLessThanTwoDamageInTotal() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageWhenOneTargetLeaves() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix(), new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 4);
        Permanent cub = harness.addToBattlefieldAndReturn(player2, new LongtuskCub());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, Map.of(cub.getId(), 1, player2.getId(), 1));
        harness.castInstant(player1, 0, Map.of(cub.getId(), 2));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Longtusk Cub");
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent cub1 = harness.addToBattlefieldAndReturn(player2, new LongtuskCub());
        Permanent cub2 = harness.addToBattlefieldAndReturn(player2, new LongtuskCub());
        Permanent cub3 = harness.addToBattlefieldAndReturn(player2, new LongtuskCub());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(
                cub1.getId(), 1,
                cub2.getId(), 1,
                cub3.getId(), 1
        ))).isInstanceOf(IllegalStateException.class);
    }
}
