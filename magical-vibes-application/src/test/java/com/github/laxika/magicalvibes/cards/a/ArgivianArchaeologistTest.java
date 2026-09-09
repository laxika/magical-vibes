package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgivianArchaeologist.class, Forest.class, Memnite.class})
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
}
