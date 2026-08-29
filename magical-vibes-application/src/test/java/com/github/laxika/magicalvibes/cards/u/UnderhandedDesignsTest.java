package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnderhandedDesignsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} drains each opponent when an artifact you control enters")
    void payingOneManaDrainsEachOpponent() {
        addReadyDesigns(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the artifact-entry payment has no effect")
    void decliningPaymentHasNoEffect() {
        addReadyDesigns(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrificing Underhanded Designs destroys a target creature with two artifacts")
    void sacrificesAndDestroysTargetCreature() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        addArtifactReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Underhanded Designs");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without two artifacts")
    void cannotActivateWithoutTwoArtifacts() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDesigns(Player player) {
        Permanent permanent = new Permanent(new UnderhandedDesigns());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addArtifactReady(Player player) {
        Permanent permanent = new Permanent(new Ornithopter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
