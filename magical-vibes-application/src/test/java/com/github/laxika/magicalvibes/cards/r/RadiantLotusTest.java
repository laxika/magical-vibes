package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RadiantLotusTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing artifacts adds three mana per artifact to the target player")
    void sacrificesArtifactsAndAddsManaToTargetPlayer() {
        Permanent lotus = harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        Permanent petal1 = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        Permanent petal2 = harness.addToBattlefieldAndReturn(player1, new LotusPetal());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.handlePermanentChosen(player1, petal1.getId());
        harness.handlePermanentChosen(player1, petal2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE))
                .isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                .isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lotus);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Lotus Petal")))
                .hasSize(2);
        assertThat(lotus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires at least one artifact to be sacrificed")
    void requiresAtLeastOneArtifact() {
        harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void rejectsNonPlayerTarget() {
        harness.addToBattlefieldAndReturn(player1, new RadiantLotus());
        harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
