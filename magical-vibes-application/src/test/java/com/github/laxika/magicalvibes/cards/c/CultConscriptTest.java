package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CultConscript.class, DrudgeSkeletons.class, GrizzlyBears.class, Shock.class})
class CultConscriptTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CultConscript()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cult Conscript").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns from the graveyard after a non-Skeleton creature you controlled died")
    void returnsAfterNonSkeletonCreatureDiesUnderYourControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CultConscript conscript = new CultConscript();
        harness.setGraveyard(player1, List.of(conscript));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        addReturnMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(conscript.getId())
                        && permanent.isTapped());
    }

    @Test
    @DisplayName("Cannot return after only a Skeleton you controlled died")
    void cannotReturnAfterSkeletonDies() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.setGraveyard(player1, List.of(new CultConscript()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, skeleton.getId());
        harness.passBothPriorities();
        addReturnMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Skeleton creature died under your control");
    }

    @Test
    @DisplayName("Cannot return after a non-Skeleton creature an opponent controlled died")
    void cannotReturnAfterOpponentControlsDeath() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CultConscript()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        addReturnMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Skeleton creature died under your control");
    }

    private void addReturnMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
