package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuriokSalvagersTest extends BaseCardTest {

    @Test
    void returnsTargetArtifactWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card artifact = new Memnite();
        Card expensiveArtifact = new BronzeSable();
        Card nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, expensiveArtifact, nonArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(expensiveArtifact, nonArtifact);
    }

    @Test
    void cannotTargetArtifactWithManaValueGreaterThanOne() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card artifact = new BronzeSable();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetArtifactInOpponentsGraveyard() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card artifact = new Memnite();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
