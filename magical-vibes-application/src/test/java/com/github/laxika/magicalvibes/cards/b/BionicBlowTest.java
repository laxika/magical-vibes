package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BionicBlow.class, GrizzlyBears.class, HillGiant.class})
class BionicBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the creature by X before dealing damage with its boosted power")
    void boostsBeforeDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(source, victim, 1);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The damage target is optional")
    void damageTargetIsOptional() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BionicBlow()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 1, List.of(source.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
    }

    @Test
    @DisplayName("The temporary boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(source, null, 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot choose the boosted creature as the damage target")
    void targetMustBeAnotherCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BionicBlow()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent source, Permanent victim, int x) {
        harness.setHand(player1, List.of(new BionicBlow()));
        harness.addMana(player1, ManaColor.RED, x + 2);
        UUID[] targets = victim == null
                ? new UUID[]{source.getId()}
                : new UUID[]{source.getId(), victim.getId()};
        harness.castSorcery(player1, 0, x, List.of(targets));
        harness.passBothPriorities();
    }
}
