package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoraciousVampireTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a Vampire you control +1/+1 and menace")
    void etbBoostsVampireAndGrantsMenace() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID vampireId = harness.getPermanentId(player1, "Vampire Interloper");
        harness.castCreature(player1, 0, List.of(vampireId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getPowerModifier()).isEqualTo(1);
        assertThat(vampire.getToughnessModifier()).isEqualTo(1);
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("ETB boost and menace wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID vampireId = harness.getPermanentId(player1, "Vampire Interloper");
        harness.castCreature(player1, 0, List.of(vampireId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(vampireId))
                .findFirst().orElseThrow();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isZero();
        assertThat(vampire.getToughnessModifier()).isZero();
        assertThat(vampire.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }

    @Test
    @DisplayName("Rejects a non-Vampire target")
    void rejectsNonVampireTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Vampire creature you control");
    }

    @Test
    @DisplayName("Rejects an opponent's Vampire target")
    void rejectsOpponentsVampireTarget() {
        harness.addToBattlefield(player2, new VampireInterloper());
        harness.setHand(player1, List.of(new VoraciousVampire()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID vampireId = harness.getPermanentId(player2, "Vampire Interloper");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(vampireId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Vampire creature you control");
    }
}
