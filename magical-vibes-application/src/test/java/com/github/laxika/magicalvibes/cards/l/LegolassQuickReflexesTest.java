package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegolassQuickReflexes.class, GrizzlyBears.class, ColossalDreadmaw.class, Forest.class})
class LegolassQuickReflexesTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target and its tap trigger deals damage equal to its power")
    void untapsAndDealsPowerDamage() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());
        cast(target);

        assertThat(target.isTapped()).isFalse();

        tap(target);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The tap trigger may be declined")
    void mayDeclineDamageTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());
        cast(target);

        tap(target);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The spell can target only a creature")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LegolassQuickReflexes()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new LegolassQuickReflexes()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
