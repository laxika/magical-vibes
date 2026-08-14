package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalvagingStationTest extends BaseCardTest {

    @Test
    void returnsTargetNoncreatureArtifactWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new SalvagingStation());
        Card returnedCard = new TormodsCrypt();
        Card otherEligibleCard = new ChromaticStar();
        Card creatureArtifact = new Memnite();
        Card expensiveArtifact = new IsochronScepter();
        harness.setGraveyard(player1, List.of(returnedCard, otherEligibleCard, creatureArtifact, expensiveArtifact));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tormod's Crypt");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(otherEligibleCard, creatureArtifact, expensiveArtifact);
    }

    @Test
    void cannotTargetIneligibleArtifactOrOpponentGraveyard() {
        harness.addToBattlefield(player1, new SalvagingStation());
        Card creatureArtifact = new Memnite();
        Card expensiveArtifact = new IsochronScepter();
        Card opponentArtifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(creatureArtifact, expensiveArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creatureArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(expensiveArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayUntapWhenAcreatureDies() {
        Permanent station = addReadyStation(player1);
        station.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    private Permanent addReadyStation(Player player) {
        Permanent station = new Permanent(new SalvagingStation());
        station.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(station);
        return station;
    }
}
