package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RenegadeSilent;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NanogeneConversion.class, GrizzlyBears.class, HillGiant.class})
class NanogeneConversionTest extends BaseCardTest {

    @Test
    @DisplayName("Makes every other creature a copy of the target")
    void makesOtherCreaturesCopies() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        cast(target);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @CardUsed(ThrunTheLastTroll.class)
    @DisplayName("Removes legendary from the copied creatures")
    void removesLegendaryFromCopies() {
        Permanent target = addCreatureReady(player1, new ThrunTheLastTroll());
        Permanent copy = addCreatureReady(player1, new GrizzlyBears());

        cast(target);

        assertThat(target.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Copies revert at the end of the turn")
    void copiesRevertAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent copy = addCreatureReady(player1, new GrizzlyBears());

        cast(target);
        assertThat(copy.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(copy.getEffectivePower()).isEqualTo(2);
        assertThat(copy.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetAnOpposingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NanogeneConversion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new NanogeneConversion()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @CardUsed(ThrunTheLastTroll.class)
    @DisplayName("Copies face-down characteristics rather than the hidden card")
    void copiesFaceDownCharacteristics() {
        Permanent target = addCreatureReady(player1, new ThrunTheLastTroll());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent copy = addCreatureReady(player2, new HillGiant());

        cast(target);

        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HEXPROOF)).isFalse();
        assertThat(copy.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Copies do not acquire the target's counters and retain their own counters and tapped state")
    void retainsOwnCountersAndStatus() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent copy = addCreatureReady(player2, new GrizzlyBears());
        copy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        copy.setTapped(true);

        cast(target);

        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
        assertThat(copy.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(copy.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void laterCreaturesAreUnaffected() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        cast(target);

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not copy anything if the target changes controllers before resolution")
    void targetMustStillBeControlledOnResolution() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NanogeneConversion()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Nanogene Conversion");
    }

    @Test
    @CardUsed(RenegadeSilent.class)
    @DisplayName("The copy expires even when a copied Renegade Silent phases out before cleanup")
    void phasedOutCopyRevertsAtCleanup() {
        Permanent target = addCreatureReady(player1, new RenegadeSilent());
        Permanent copy = addCreatureReady(player1, new GrizzlyBears());
        cast(target);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(copy);
        harness.passBothPriorities();
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);
        assertThat(copy.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
    }

    @Test
    @DisplayName("Repeated conversions copy existing copy values and restore the original creatures at cleanup")
    void repeatedConversionsRevertToOriginalCards() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent ownCopy = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCopy = addCreatureReady(player2, new GrizzlyBears());

        cast(target);
        cast(ownCopy);

        assertThat(gqs.getEffectivePower(gd, opposingCopy)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownCopy)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCopy)).isEqualTo(2);
    }
}
