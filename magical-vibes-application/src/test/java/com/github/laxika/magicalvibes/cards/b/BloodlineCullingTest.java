package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
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

@CardUsed({BloodlineCulling.class, AvatarOfMight.class, FountainOfYouth.class, GrizzlyBears.class,
        YavimayaSapherd.class})
class BloodlineCullingTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -5/-5 until end of turn")
    void targetCreatureGetsMinusFiveMinusFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        castBloodlineCulling(0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The targeted creature's -5/-5 wears off at end of turn")
    void targetedCreatureBoostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        castBloodlineCulling(0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Mode 1 gives creature tokens -2/-2 and leaves nontoken creatures alone")
    void weakensCreatureTokensOnly() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Saproling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent nontoken = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBloodlineCulling(1, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(nontoken.getEffectivePower()).isEqualTo(2);
        assertThat(nontoken.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The targeted mode cannot target a noncreature permanent")
    void targetedModeRejectsNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new BloodlineCulling()));
        addBloodlineCullingMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castBloodlineCulling(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BloodlineCulling()));
        addBloodlineCullingMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addBloodlineCullingMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
