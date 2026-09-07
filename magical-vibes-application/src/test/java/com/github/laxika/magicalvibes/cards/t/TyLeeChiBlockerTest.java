package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyLeeChiBlocker.class, GrizzlyBears.class, Shock.class})
class TyLeeChiBlockerTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Ty Lee +1/+1 until end of turn")
    void prowessPumpsUntilEndOfTurn() {
        Permanent tyLee = castTyLee(null);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tyLee)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tyLee)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tyLee)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tyLee)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotTriggerProwess() {
        Permanent tyLee = castTyLee(null);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, tyLee)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tyLee)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ty Lee taps the optional target and prevents its untap")
    void tapsAndLocksOptionalTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTyLee(bears.getId());

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    @DisplayName("Ty Lee's untap lock ends when Ty Lee leaves the battlefield")
    void untapLockEndsWhenTyLeeLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tyLee = castTyLee(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(tyLee);
        bears.tap();
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    private Permanent castTyLee(UUID targetId) {
        harness.setHand(player1, List.of(new TyLeeChiBlocker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        if (targetId == null) {
            harness.castCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0, 0, targetId);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities();
        }
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TyLeeChiBlocker)
                .findFirst()
                .orElseThrow();
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
