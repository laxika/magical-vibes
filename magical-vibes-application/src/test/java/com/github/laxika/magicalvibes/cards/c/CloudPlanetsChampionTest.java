package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudPlanetsChampion.class, LeoninScimitar.class})
class CloudPlanetsChampionTest extends BaseCardTest {

    @Test
    void withoutEquipmentDoesNotHaveDoubleStrikeOrIndestructible() {
        Permanent cloud = addCloudReady(player1);

        assertThat(gqs.hasKeyword(gd, cloud, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cloud, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void whileEquippedDuringYourTurnHasDoubleStrikeAndIndestructible() {
        harness.forceActivePlayer(player1);
        Permanent cloud = addCloudReady(player1);
        attachScimitarTo(cloud, player1);

        assertThat(gqs.hasKeyword(gd, cloud, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cloud, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void whileEquippedDuringOpponentsTurnDoesNotHaveDoubleStrikeOrIndestructible() {
        Permanent cloud = addCloudReady(player1);
        attachScimitarTo(cloud, player1);
        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, cloud, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cloud, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void equipmentTargetingCloudCostsTwoLessToActivate() {
        Permanent cloud = addCloudReady(player1);
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, cloud.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(cloud.getId());
    }

    private Permanent addCloudReady(Player player) {
        Permanent cloud = new Permanent(new CloudPlanetsChampion());
        cloud.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(cloud);
        return cloud;
    }

    private void attachScimitarTo(Permanent cloud, Player player) {
        Permanent scimitar = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        scimitar.setAttachedTo(cloud.getId());
    }
}
