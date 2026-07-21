package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetallurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to target artifact")
    void resolvingGrantsShield() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's artifact")
    void canTargetOpponentArtifact() {
        setupMetallurgeon();
        Permanent opponentArtifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Metallurgeon")
    void tapsOnActivation() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(findPermanent(player1, "Metallurgeon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifact() {
        setupMetallurgeon();
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        setupMetallurgeon();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can regenerate itself as an artifact creature")
    void canRegenerateSelf() {
        Permanent metallurgeon = setupMetallurgeon();

        harness.activateAbility(player1, 0, null, metallurgeon.getId());
        harness.passBothPriorities();

        assertThat(metallurgeon.getRegenerationShield()).isEqualTo(1);
        assertThat(metallurgeon.isTapped()).isTrue();
    }

    private Permanent setupMetallurgeon() {
        harness.addToBattlefield(player1, new Metallurgeon());
        Permanent metallurgeon = findPermanent(player1, "Metallurgeon");
        metallurgeon.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        return metallurgeon;
    }

    private Permanent addArtifact(Player player) {
        Card artifactCard = new Card();
        artifactCard.setName("Test Artifact");
        artifactCard.setType(CardType.ARTIFACT);

        Permanent permanent = new Permanent(artifactCard);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
