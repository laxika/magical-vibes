package com.github.laxika.magicalvibes.cards.l;

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

class LoxodonMenderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to target artifact")
    void resolvingGrantsShield() {
        setupLoxodonMender();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's artifact")
    void canTargetOpponentArtifact() {
        setupLoxodonMender();
        Permanent opponentArtifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Loxodon Mender")
    void tapsOnActivation() {
        Permanent mender = setupLoxodonMender();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(mender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        setupLoxodonMender();
        Card nonArtifactCard = new Card();
        nonArtifactCard.setName("Test Permanent");
        nonArtifactCard.setType(CardType.CREATURE);
        Permanent nonArtifact = new Permanent(nonArtifactCard);
        gd.playerBattlefields.get(player1.getId()).add(nonArtifact);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        setupLoxodonMender();
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent setupLoxodonMender() {
        harness.addToBattlefield(player1, new LoxodonMender());
        Permanent mender = findPermanent(player1, "Loxodon Mender");
        mender.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        return mender;
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
