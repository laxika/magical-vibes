package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagetaTheLionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all other creatures on both battlefields and cannot be regenerated")
    void destroysAllOtherCreaturesWithoutRegeneration() {
        Permanent mageta = harness.addToBattlefieldAndReturn(player1, new MagetaTheLion());
        mageta.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mageta);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(mageta.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without two cards to discard")
    void cannotActivateWithoutTwoCards() {
        Permanent mageta = harness.addToBattlefieldAndReturn(player1, new MagetaTheLion());
        mageta.setSummoningSick(false);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mageta.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
