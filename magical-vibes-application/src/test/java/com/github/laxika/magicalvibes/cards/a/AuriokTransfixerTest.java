package com.github.laxika.magicalvibes.cards.a;

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

class AuriokTransfixerTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: Tap target artifact taps the chosen artifact")
    void tapAbilityTapsTargetArtifact() {
        Permanent transfixer = addReadyTransfixer(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(transfixer.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can target an artifact you control")
    void canTargetOwnArtifact() {
        addReadyTransfixer(player1);
        Permanent artifact = addArtifact(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        addReadyTransfixer(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate tap ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyTransfixer(player1);
        Permanent artifact = addArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyTransfixer(Player player) {
        Permanent perm = new Permanent(new AuriokTransfixer());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        Card card = new Card();
        card.setName("Test Artifact");
        card.setType(CardType.ARTIFACT);
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
