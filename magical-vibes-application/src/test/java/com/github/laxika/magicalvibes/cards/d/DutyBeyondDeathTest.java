package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DutyBeyondDeath.class, GrizzlyBears.class})
class DutyBeyondDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting requires sacrificing a creature")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new DutyBeyondDeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Resolving sacrifices one creature and affects the remaining creatures you control")
    void sacrificesAndBuffsControlledCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DutyBeyondDeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(survivor.getEffectivePower()).isEqualTo(3);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn while counters remain")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DutyBeyondDeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(survivor.getEffectivePower()).isEqualTo(3);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The sacrifice is paid before resolution, even when it is your only creature")
    void canSacrificeOnlyCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DutyBeyondDeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Duty Beyond Death");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Duty Beyond Death");
    }

    @Test
    @DisplayName("Creatures entering before resolution receive both effects")
    void affectsCreaturesPresentAtResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DutyBeyondDeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither effect")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DutyBeyondDeath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
