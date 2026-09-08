package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Tap target artifact taps the chosen artifact")
    void tapAbilityTapsTargetArtifact() {
        Permanent barrier = addReadyBarrier(player1);
        Permanent artifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(barrier.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can target an artifact you control")
    void canTargetOwnArtifact() {
        addReadyBarrier(player1);
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        addReadyBarrier(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBarrier(Player player) {
        Permanent perm = new Permanent(new RelicBarrier());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        Card card = new AngelsFeather();
        card.setName("Test Artifact");
        card.setType(CardType.ARTIFACT);
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
