package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed(LightningBerserker.class)
class LightningBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} gives Lightning Berserker +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new LightningBerserker());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent berserker = findPermanent(player1, "Lightning Berserker");
        assertThat(berserker.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanent(player1, "Lightning Berserker")).isSameAs(berserker);
    }

    @Test
    @DisplayName("Dash grants haste and returns Lightning Berserker to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent berserker = findPermanent(player1, "Lightning Berserker");
        assertThat(berserker.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Lightning Berserker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Berserker");
        harness.assertNotOnBattlefield(player1, "Lightning Berserker");
    }

    @Test
    @DisplayName("Resolving a dashed creature does not create an enters-the-battlefield trigger")
    void dashReturnIsScheduledByTheSpellWithoutAnEtbTrigger() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Berserker");
        assertThat(gd.stack).isEmpty();
    }
}
