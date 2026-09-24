package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hammerhand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvyGleefulSpellthief.class, GiantGrowth.class, DualShot.class, GrizzlyBears.class, Hammerhand.class})
class IvyGleefulSpellthiefTest extends BaseCardTest {

    @Test
    @DisplayName("Copies another player's single-creature-targeting spell onto Ivy")
    void copiesSpellTargetingAnotherCreature() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
        assertThat(copy.getTargetId()).isEqualTo(ivy.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.passBothPriorities();

        assertThat(ivy.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a spell with two creature targets")
    void doesNotCopyMultipleCreatureTargets() {
        harness.addToBattlefield(player1, new IvyGleefulSpellthief());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(firstBear.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the spell targets Ivy")
    void doesNotCopySpellTargetingIvy() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, ivy.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ivy.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Copies a spell with repeated target occurrences onto Ivy")
    void copiesRepeatedTargetOccurrences() {
        Permanent ivy = harness.addToBattlefieldAndReturn(player1, new IvyGleefulSpellthief());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Hammerhand()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castEnchantment(player2, 0, List.of(bear.getId(), bear.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getTargetId()).isEqualTo(ivy.getId());
        assertThat(copy.getDeclaredTargetIds()).containsExactly(ivy.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Hammerhand");
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getAttachedTo()).isEqualTo(ivy.getId());
                });
    }
}
