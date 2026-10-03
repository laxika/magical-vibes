package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgivianArchaeologist.class, Forest.class, GrizzlyBears.class, Memnite.class, TormodsCrypt.class})
class ArgivianArchaeologistTest extends BaseCardTest {

    @Test
    void returnsTargetArtifactFromOwnGraveyardToHand() {
        Permanent archaeologist = addReadyArchaeologist();
        Card artifact = new Memnite();
        Card nonArtifact = new Forest();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
        assertThat(archaeologist.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonArtifactCard() {
        addReadyArchaeologist();
        Card nonArtifact = new Forest();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetArtifactInOpponentsGraveyard() {
        addReadyArchaeologist();
        Card artifact = new Memnite();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArchaeologist() {
        Permanent archaeologist = harness.addToBattlefieldAndReturn(player1, new ArgivianArchaeologist());
        archaeologist.setSummoningSick(false);
        return archaeologist;
    }


    @Test
    void returnsTargetArtifactFromGraveyardToHand() {
        addReadyArchaeologist();
        Card artifact = new TormodsCrypt();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tormod's Crypt");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void cannotTargetNonArtifactOrOpponentGraveyard() {
        addReadyArchaeologist();
        Card nonArtifact = new GrizzlyBears();
        Card opponentArtifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void cannotActivateWithOnlyOneWhiteMana() {
        Permanent archaeologist = addReadyArchaeologist();
        Card artifact = new Memnite();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(archaeologist.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent archaeologist = addReadyArchaeologist();
        archaeologist.tap();
        Card artifact = new Memnite();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent archaeologist = addReadyArchaeologist();
        archaeologist.setSummoningSick(true);
        Card artifact = new Memnite();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(archaeologist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReturnArtifactExiledInResponse() {
        Permanent archaeologist = addReadyArchaeologist();
        Card artifact = new Memnite();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
        assertThat(archaeologist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesAfterArchaeologistLeavesBattlefield() {
        Permanent archaeologist = addReadyArchaeologist();
        Card artifact = new Memnite();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, archaeologist);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(archaeologist.getCard());
        harness.assertNotOnBattlefield(player1, "Argivian Archaeologist");
        assertThat(gd.stack).isEmpty();
    }

}
