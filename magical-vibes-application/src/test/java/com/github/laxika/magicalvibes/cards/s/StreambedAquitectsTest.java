package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 2: turn a land into an Island =====

    @Test
    @DisplayName("Resolving the second ability makes the target land an Island")
    void landBecomesIsland() {
        addCreatureReady(player1, new StreambedAquitects());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, 1, null, forestId);
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Granted Island type wears off at end of turn")
    void islandTypeWearsOff() {
        addCreatureReady(player1, new StreambedAquitects());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, 1, null, forestId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientSubtypes()).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Second ability cannot target a creature")
    void landAbilityCannotTargetCreature() {
        addCreatureReady(player1, new StreambedAquitects());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
