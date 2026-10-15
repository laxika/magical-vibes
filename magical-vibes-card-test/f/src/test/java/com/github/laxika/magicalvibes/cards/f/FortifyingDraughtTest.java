package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortifyingDraught.class, GrizzlyBears.class, FountainOfYouth.class})
class FortifyingDraughtTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life and gives +2/+2 when no prior life was gained this turn")
    void gainsLifeAndGivesTwoTwoWithNoPriorGain() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 22);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Includes prior life gained this turn plus the 2 from the spell in X")
    void includesPriorLifeGainedPlusOwnGain() {
        Permanent target = addCreature(player2);
        gd.lifeGainedThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 22);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
    }

    @Test
    @DisplayName("Does not count opponent's life gained this turn")
    void ignoresOpponentLifeGained() {
        Permanent target = addCreature(player2);
        gd.lifeGainedThisTurn.put(player2.getId(), 7);
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The +X/+X wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("No life is gained when the only target has left the battlefield")
    void gainsNoLifeWhenTargetIsGone() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Fortifying Draught");
    }

    @Test
    @DisplayName("Successive casts count actual life gain and do not recalculate an earlier boost")
    void successiveCastsUseCumulativeGainWithFixedEarlierBoost() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player2);
        harness.setHand(player1, List.of(new FortifyingDraught(), new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.castAndResolveInstant(player1, 0, second.getId());

        harness.assertLife(player1, 24);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
