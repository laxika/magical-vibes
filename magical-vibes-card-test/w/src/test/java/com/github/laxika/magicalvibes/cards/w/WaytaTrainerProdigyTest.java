package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RipjawRaptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaytaTrainerProdigy.class, RipjawRaptor.class, GrizzlyBears.class})
class WaytaTrainerProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the fight ability to {G} for two creatures you control and doubles damage triggers")
    void discountsOwnTargetsAndDoublesDamageTrigger() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent raptor = addCreatureReady(player1, new RipjawRaptor());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(wayta), 0,
                List.of(raptor.getId(), bear.getId()));

        assertThat(wayta.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(raptor.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Does not reduce the cost when the second creature is controlled by an opponent")
    void requiresBothTargetsToBeControlled() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent raptor = addCreatureReady(player1, new RipjawRaptor());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, battlefieldIndex(wayta), 0, List.of(raptor.getId(), opposingBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wayta.isTapped()).isFalse();
    }

    @Test
    void fullCostFightDoublesOnlyOurDamageTrigger() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent ourRaptor = addCreatureReady(player1, new RipjawRaptor());
        Permanent opposingRaptor = addCreatureReady(player2, new RipjawRaptor());
        harness.setLibrary(player1, List.of(new RipjawRaptor(), new RipjawRaptor()));
        harness.setLibrary(player2, List.of(new RipjawRaptor(), new RipjawRaptor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int ourHandBefore = gd.playerHands.get(player1.getId()).size();
        int opposingHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(wayta), 0,
                List.of(ourRaptor.getId(), opposingRaptor.getId()));
        harness.passBothPriorities();

        assertThat(wayta.isTapped()).isTrue();
        assertThat(ourRaptor.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingRaptor.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ourHandBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opposingHandBefore + 1);
    }

    @Test
    void doublesEachFriendlyCreaturesDamageTrigger() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent first = addCreatureReady(player1, new RipjawRaptor());
        Permanent second = addCreatureReady(player1, new RipjawRaptor());
        harness.setLibrary(player1, List.of(new RipjawRaptor(), new RipjawRaptor(),
                new RipjawRaptor(), new RipjawRaptor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(wayta), 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(4);
        assertThat(second.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }

    @Test
    void canFightUsingWaytaAsFirstTargetOnItsEntryTurn() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        wayta.setSummoningSick(true);
        Permanent raptor = addCreatureReady(player1, new RipjawRaptor());
        harness.setLibrary(player1, List.of(new RipjawRaptor(), new RipjawRaptor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(wayta), 0,
                List.of(wayta.getId(), raptor.getId()));
        harness.passBothPriorities();

        assertThat(wayta.isTapped()).isTrue();
        assertThat(wayta.getMarkedDamage()).isEqualTo(4);
        assertThat(raptor.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void rejectsTheSameCreatureForBothTargets() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent raptor = addCreatureReady(player1, new RipjawRaptor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, battlefieldIndex(wayta), 0, List.of(raptor.getId(), raptor.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wayta.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsAnOpposingCreatureAsFirstTarget() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent ourRaptor = addCreatureReady(player1, new RipjawRaptor());
        Permanent opposingRaptor = addCreatureReady(player2, new RipjawRaptor());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, battlefieldIndex(wayta), 0,
                List.of(opposingRaptor.getId(), ourRaptor.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wayta.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noFightOccursWhenOneTargetLeavesBeforeResolution() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent first = addCreatureReady(player1, new RipjawRaptor());
        Permanent second = addCreatureReady(player1, new RipjawRaptor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(wayta), 0,
                List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(wayta.isTapped()).isTrue();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
