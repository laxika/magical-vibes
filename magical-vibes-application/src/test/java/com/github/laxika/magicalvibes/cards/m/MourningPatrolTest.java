package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FlipTheSwitch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MourningPatrol.class, MorningApparition.class, FlipTheSwitch.class})
class MourningPatrolTest extends BaseCardTest {

    @Test
    void frontFaceGoesToGraveyardNormally() {
        MourningPatrol patrol = new MourningPatrol();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, patrol);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(patrol);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void counteredDisturbSpellIsExiled() {
        MourningPatrol patrol = new MourningPatrol();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(patrol));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new FlipTheSwitch()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, patrol.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(patrol.getId());
    }

    @Test
    void disturbRequiresFourManaRatherThanFrontFaceCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MourningPatrol()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void disturbCannotBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MourningPatrol()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Disturb casts Mourning Patrol from the graveyard transformed as Morning Apparition")
    void disturbEntersTransformed() {
        Permanent apparition = castWithDisturb();

        assertThat(apparition.isTransformed()).isTrue();
        assertThat(apparition.getCard().getName()).isEqualTo("Morning Apparition");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Morning Apparition is exiled instead of going to the graveyard")
    void apparitionIsExiledInsteadOfGraveyard() {
        Permanent apparition = castWithDisturb();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, apparition));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(apparition.getOriginalCard().getId());
    }

    private Permanent castWithDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new MourningPatrol()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
