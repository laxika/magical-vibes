package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlagueSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the nonblack creature and land without allowing regeneration")
    void destroysNonblackCreatureAndLandWithoutRegeneration() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        creature.setRegenerationShield(1);
        land.setRegenerationShield(1);
        prepareCast();

        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent as the second target")
    void cannotTargetNonlandAsSecondTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), otherCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new PlagueSpores()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
