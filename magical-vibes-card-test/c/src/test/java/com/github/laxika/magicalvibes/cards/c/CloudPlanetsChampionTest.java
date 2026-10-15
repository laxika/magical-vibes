package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.g.GenjiGlove;
import com.github.laxika.magicalvibes.cards.s.SimianSling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudPlanetsChampion.class, LeoninScimitar.class, GenjiGlove.class, SimianSling.class})
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

    @Test
    void losesGrantedKeywordsWhenLastEquipmentIsRemoved() {
        harness.forceActivePlayer(player1);
        Permanent cloud = addCloudReady(player1);
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(cloud.getId());

        assertThat(gqs.hasKeyword(gd, cloud, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cloud, Keyword.INDESTRUCTIBLE)).isTrue();

        scimitar.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, cloud, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cloud, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void equipCostAboveTwoStillRequiresTheRemainingMana() {
        harness.forceActivePlayer(player1);
        Permanent cloud = addCloudReady(player1);
        Permanent glove = harness.addToBattlefieldAndReturn(player1, new GenjiGlove());
        int gloveIndex = gd.playerBattlefields.get(player1.getId()).indexOf(glove);

        assertThatThrownBy(() -> harness.activateAbility(player1, gloveIndex, null, cloud.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gloveIndex, null, cloud.getId());
        harness.passBothPriorities();

        assertThat(glove.getAttachedTo()).isEqualTo(cloud.getId());
    }

    @Test
    void equipTargetingAnotherCreatureIsNotDiscounted() {
        harness.forceActivePlayer(player1);
        addCloudReady(player1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SimianSling());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        int scimitarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scimitar);

        assertThatThrownBy(() -> harness.activateAbility(player1, scimitarIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, scimitarIndex, null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(otherCreature.getId());
    }

    @Test
    void reconfigureTargetingCloudIsNotDiscounted() {
        harness.forceActivePlayer(player1);
        Permanent cloud = addCloudReady(player1);
        Permanent sling = harness.addToBattlefieldAndReturn(player1, new SimianSling());
        int slingIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sling);

        assertThatThrownBy(() -> harness.activateAbility(player1, slingIndex, null, cloud.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, slingIndex, null, cloud.getId());
        harness.passBothPriorities();

        assertThat(sling.getAttachedTo()).isEqualTo(cloud.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addCloudReady(Player player) {
        Permanent cloud = harness.addToBattlefieldAndReturn(player, new CloudPlanetsChampion());
        cloud.setSummoningSick(false);
        return cloud;
    }

    private void attachScimitarTo(Permanent cloud, Player player) {
        Permanent scimitar = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        scimitar.setAttachedTo(cloud.getId());
    }
}
