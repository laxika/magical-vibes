package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DissectionPracticeTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the opponent lose life, makes you gain life, and applies both creature effects")
    void resolvesAllEffects() {
        Permanent boosted = addCreature(player2);
        Permanent weakened = addCreature(player2);

        cast(List.of(player2.getId(), boosted.getId(), weakened.getId()));

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        assertThat(weakened.getPowerModifier()).isEqualTo(-1);
        assertThat(weakened.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Allows the two optional creature targets to be the same creature")
    void allowsSharedCreatureTarget() {
        Permanent creature = addCreature(player2);

        cast(List.of(player2.getId(), creature.getId(), creature.getId()));

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can resolve with neither optional creature target")
    void resolvesWithoutCreatureTargets() {
        cast(List.of(player2.getId()));

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Creature modifications expire at end of turn")
    void creatureModificationsExpireAtEndOfTurn() {
        Permanent creature = addCreature(player2);

        cast(List.of(player2.getId(), creature.getId()));

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);

        gd.expireEndOfTurnFloatingEffects();
        creature.resetModifiers();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rejects a non-opponent as the first target")
    void rejectsNonOpponentTarget() {
        Permanent creature = addCreature(player2);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player1.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature as an optional creature target")
    void rejectsNonCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player2.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DissectionPractice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
