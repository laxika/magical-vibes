package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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

@CardUsed({BlightGrenade.class, GrizzlyBears.class, Plains.class, SerraAngel.class})
class BlightGrenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target creature and gives all other creatures -3/-3")
    void destroysTargetAndWeakensAllCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castBlightGrenade(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, ownAngel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownAngel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingAngel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingAngel)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void weakensUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castBlightGrenade(target.getId());
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BlightGrenade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castBlightGrenade(UUID targetId) {
        harness.setHand(player1, List.of(new BlightGrenade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
