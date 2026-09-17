package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollenRemedy.class, AncientSpider.class, MeteorCrater.class})
class PollenRemedyTest extends BaseCardTest {

    @Test
    void preventsThreeDamageDividedAmongCreatureAndPlayer() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, Map.of(spider.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(spider.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gameData.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void kickedPreventsSixDamageAndSacrificesALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, Map.of(spider.getId(), 3, player2.getId(), 3),
                List.of(), List.of(), false, land.getId(), null, null, null, null, true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(spider.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gameData.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
        harness.assertInGraveyard(player1, "Meteor Crater");
    }

    @Test
    void preventionAssignmentsMustSumToThree() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(spider.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Prevention assignments must sum to 3");
    }

    @Test
    void kickerRequiresSacrificingALand() {
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 3), List.of(), List.of(), false, nonland.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayChooseZeroTargets() {
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, Map.<UUID, Integer>of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pollen Remedy");
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }
}
