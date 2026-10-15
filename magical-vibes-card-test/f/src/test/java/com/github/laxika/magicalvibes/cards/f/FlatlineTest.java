package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flatline.class, GrizzlyBears.class})
class FlatlineTest extends BaseCardTest {

    @Test
    @DisplayName("Sets opponents' creatures to base 0/1 until end of turn")
    void setsOpponentsCreaturesBasePowerAndToughness() {
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        castFlatline();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isZero();
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The base power and toughness setting wears off at end of turn")
    void settingWearsOffAtEndOfTurn() {
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        castFlatline();
        assertThat(opponentCreature.getEffectivePower()).isZero();
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sets every opposing creature's base stats")
    void affectsMultipleOpposingCreatures() {
        Permanent first = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        castFlatline();

        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters still modify the new base stats")
    void preservesCounterBonuses() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castFlatline();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectLaterCreatures() {
        Permanent existing = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        castFlatline();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existing)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves without opposing creatures or targets")
    void resolvesWithoutOpposingCreatures() {
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        castFlatline();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flatline");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    private void castFlatline() {
        harness.setHand(player1, List.of(new Flatline()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
