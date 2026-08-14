package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelJiladLifebreatherTest extends BaseCardTest {

    @Test
    void sacrificesAForestAndRegeneratesTargetCreature() {
        Permanent lifebreather = addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(lifebreather.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void cannotActivateWithoutAForest() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
