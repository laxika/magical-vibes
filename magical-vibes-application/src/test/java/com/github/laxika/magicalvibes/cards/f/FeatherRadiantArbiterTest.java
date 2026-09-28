package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hammerhand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeatherRadiantArbiter.class, GiantGrowth.class, GrizzlyBears.class, Hammerhand.class})
class FeatherRadiantArbiterTest extends BaseCardTest {

    @Test
    void choosesCreaturesPaysPerCreatureAndCopiesSpell() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new FeatherRadiantArbiter());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, feather.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(feather.getEffectivePower()).isEqualTo(7);
        assertThat(firstBear.getEffectivePower()).isEqualTo(5);
        assertThat(secondBear.getEffectivePower()).isEqualTo(5);
    }

    @Test
    void permanentSpellCopyBecomesToken() {
        Permanent feather = harness.addToBattlefieldAndReturn(player1, new FeatherRadiantArbiter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, feather.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getTargetId()).isEqualTo(bear.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Hammerhand");
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getAttachedTo()).isEqualTo(bear.getId());
                });
    }

    @Test
    void doesNotTriggerForSpellTargetingAnotherCreature() {
        harness.addToBattlefield(player1, new FeatherRadiantArbiter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }
}
