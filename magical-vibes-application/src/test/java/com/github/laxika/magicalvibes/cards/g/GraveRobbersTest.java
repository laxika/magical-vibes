package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveRobbers.class, TormodsCrypt.class, BogImp.class})
class GraveRobbersTest extends BaseCardTest {

    @Test
    void exilesTargetArtifactAndGainsLife() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card artifact = new TormodsCrypt();
        Card creature = new BogImp();
        harness.setGraveyard(player2, List.of(artifact, creature));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(player1, graveRobbers), 0,
                List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(artifact);
        assertThat(graveRobbers.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonArtifactCard() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card creature = new BogImp();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(player1, graveRobbers), 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGainLifeIfTargetLeavesGraveyardBeforeResolution() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card artifact = new TormodsCrypt();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(player1, graveRobbers), 0,
                List.of(artifact.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canTargetArtifactInControllersGraveyard() {
        Permanent graveRobbers = addReadyGraveRobbers();
        Card artifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(player1, graveRobbers), 0,
                List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(artifact);
    }

    private Permanent addReadyGraveRobbers() {
        return addCreatureReady(player1, new GraveRobbers());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
