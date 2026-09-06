package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatagiaTiger.class, EliteVanguard.class, GrizzlyBears.class})
class PatagiaTigerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a Human creature you control +2/+2 until end of turn")
    void etbBoostsHumanYouControl() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        castPatagiaTiger(human.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        castPatagiaTiger(human.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's Human")
    void cannotTargetOpponentsHuman() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, human.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Human creature")
    void cannotTargetNonHumanCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, creature.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB does not trigger without a Human you control")
    void etbDoesNotTriggerWithoutLegalTarget() {
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patagia Tiger");
        assertThat(gd.stack).isEmpty();
    }

    private void castPatagiaTiger(UUID targetId) {
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
