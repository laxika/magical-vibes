package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HostileTakeover.class, GrizzlyBears.class, HillGiant.class, ColossalDreadmaw.class})
class HostileTakeoverTest extends BaseCardTest {

    @Test
    @DisplayName("Sets the two targets' base stats before dealing 3 damage to each creature")
    void setsTargetsAndDealsMassDamage() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent strengthened = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent untouched = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        cast(List.of(weakened.getId(), strengthened.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, strengthened)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, strengthened)).isEqualTo(4);
        assertThat(strengthened.getMarkedDamage()).isEqualTo(3);
        assertThat(untouched.getMarkedDamage()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, strengthened)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, strengthened)).isEqualTo(3);
        assertThat(strengthened.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can choose no targets and still deal 3 damage to each creature")
    void canChooseNoTargets() {
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent creatureThatSurvives = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        cast(List.of());

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(creatureThatSurvives.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Requires the optional targets to be different creatures")
    void rejectsSameCreatureAsBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new HostileTakeover()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
