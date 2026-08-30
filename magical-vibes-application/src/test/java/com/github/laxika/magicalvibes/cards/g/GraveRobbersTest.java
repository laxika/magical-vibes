package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveRobbers.class, TormodsCrypt.class, GrizzlyBears.class})
class GraveRobbersTest extends BaseCardTest {

    @Test
    void exilesTargetArtifactAndGainsLife() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card artifact = new TormodsCrypt();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(artifact, creature));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, graveRobbers), 0,
                null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(artifact);
        assertThat(graveRobbers.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonArtifactCard() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, graveRobbers), 0,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGainLifeIfTargetLeavesGraveyardBeforeResolution() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card artifact = new TormodsCrypt();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, graveRobbers), 0,
                null, artifact.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addReadyGraveRobbers() {
        Permanent graveRobbers = harness.addToBattlefieldAndReturn(player1, new GraveRobbers());
        graveRobbers.setSummoningSick(false);
        return graveRobbers;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
