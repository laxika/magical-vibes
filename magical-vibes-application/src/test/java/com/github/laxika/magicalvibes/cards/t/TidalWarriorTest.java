package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TidalWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability targets a land")
    void activatingAbilityTargetsLand() {
        addCreatureReady(player1, new TidalWarrior());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Resolving the ability makes the target land an Island")
    void landBecomesIsland() {
        addCreatureReady(player1, new TidalWarrior());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The granted Island type wears off at end of turn")
    void islandTypeWearsOff() {
        addCreatureReady(player1, new TidalWarrior());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientSubtypes()).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The ability cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
