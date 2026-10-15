package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreambedAquitects.class, DeeptreadMerrow.class, Forest.class,
        GoldmeadowDodger.class})
class StreambedAquitectsTest extends BaseCardTest {

    // ===== Ability 1: pump a Merfolk =====

    @Test
    @DisplayName("Activating the pump ability targets the chosen Merfolk")
    void pumpAbilityTargetsMerfolk() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());

        harness.activateAbility(player1, 0, 0, null, merrow.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(merrow.getId());
    }

    @Test
    @DisplayName("Resolving the pump grants +1/+1 and islandwalk to the Merfolk")
    void pumpGrantsBoostAndIslandwalk() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());

        harness.activateAbility(player1, 0, 0, null, merrow.getId());
        harness.passBothPriorities();

        assertThat(merrow.getPowerModifier()).isEqualTo(1);
        assertThat(merrow.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOff() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent merrow = addCreatureReady(player1, new DeeptreadMerrow());

        harness.activateAbility(player1, 0, 0, null, merrow.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(merrow.getPowerModifier()).isEqualTo(0);
        assertThat(merrow.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Pump ability cannot target a non-Merfolk creature")
    void pumpCannotTargetNonMerfolk() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent dodger = addCreatureReady(player1, new GoldmeadowDodger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, dodger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 2: turn a land into an Island =====

    @Test
    @DisplayName("Resolving the second ability makes the target land an Island")
    void landBecomesIsland() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Resolving the second ability replaces the land's existing basic land types")
    void landBecomesOnlyIsland() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsOnly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Granted Island type wears off at end of turn")
    void islandTypeWearsOff() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Second ability cannot target a creature")
    void landAbilityCannotTargetCreature() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent dodger = addCreatureReady(player1, new GoldmeadowDodger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dodger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Pump can target an opponent's Merfolk")
    void pumpCanTargetOpponentsMerfolk() {
        Permanent aquitects = addCreatureReady(player1, new StreambedAquitects());
        Permanent merrow = addCreatureReady(player2, new DeeptreadMerrow());

        harness.activateAbility(player1, 0, 0, null, merrow.getId());
        assertThat(aquitects.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(merrow.getPowerModifier()).isEqualTo(1);
        assertThat(merrow.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, merrow, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Aquitects can target itself with its pump ability")
    void pumpCanTargetItself() {
        Permanent aquitects = addCreatureReady(player1, new StreambedAquitects());

        harness.activateAbility(player1, 0, 0, null, aquitects.getId());
        harness.passBothPriorities();

        assertThat(aquitects.getPowerModifier()).isEqualTo(1);
        assertThat(aquitects.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, aquitects, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents both tap abilities")
    void summoningSicknessPreventsBothAbilities() {
        Permanent aquitects = harness.addToBattlefieldAndReturn(player1, new StreambedAquitects());
        aquitects.setSummoningSick(true);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, aquitects.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(aquitects.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Forest becomes an Island with only blue intrinsic mana")
    void opponentsLandLosesOriginalIntrinsicMana() {
        Permanent aquitects = addCreatureReady(player1, new StreambedAquitects());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        assertThat(aquitects.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.intrinsicBasicLandManaColors(gd, forest)).containsOnly(ManaColor.BLUE);
    }
}
