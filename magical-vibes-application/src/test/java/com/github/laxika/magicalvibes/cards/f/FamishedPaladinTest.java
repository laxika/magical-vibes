package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FamishedPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Famished Paladin does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent paladin = addReadyPaladin(player1);
        paladin.tap();

        advanceToNextTurn(player2);

        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Famished Paladin untaps when its controller gains life")
    void untapsWhenControllerGainsLife() {
        Permanent paladin = addReadyPaladin(player1);
        paladin.tap();
        harness.addToBattlefield(player1, new SoulWarden());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Famished Paladin does not untap when an opponent gains life")
    void doesNotUntapWhenOpponentGainsLife() {
        Permanent paladin = addReadyPaladin(player1);
        paladin.tap();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isTrue();
    }

    private Permanent addReadyPaladin(Player player) {
        Permanent permanent = new Permanent(new FamishedPaladin());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
