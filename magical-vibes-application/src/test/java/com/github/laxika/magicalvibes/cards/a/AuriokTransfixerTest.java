package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokTransfixer.class, AuriokBladewarden.class, Ornithopter.class})
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
        Permanent creature = addCreatureReady(player2, new AuriokBladewarden());
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

    @Test
    @DisplayName("Cannot activate with only colorless mana")
    void cannotActivateWithoutWhiteMana() {
        addReadyTransfixer(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Auriok Transfixer has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AuriokTransfixer());
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Auriok Transfixer is already tapped")
    void cannotActivateWhileTapped() {
        Permanent transfixer = addReadyTransfixer(player1);
        transfixer.tap();
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target an already tapped artifact")
    void canTargetAlreadyTappedArtifact() {
        addReadyTransfixer(player1);
        Permanent artifact = addArtifact(player2);
        artifact.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if the target artifact leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyTransfixer(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private Permanent addReadyTransfixer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AuriokTransfixer());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Ornithopter());
    }
}
