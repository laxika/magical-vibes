package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MireShadeTest extends BaseCardTest {

    @Test
    @DisplayName("{B}, sacrificing a Swamp puts a +1/+1 counter on it")
    void sacrificeSwampAddsCounter() {
        Permanent shade = addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("With multiple Swamps the controller chooses which one to sacrifice")
    void promptsForSwampChoice() {
        Permanent shade = addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
    }

    @Test
    @DisplayName("Cannot be activated with only a non-Swamp land to sacrifice")
    void requiresSwamp() {
        addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated outside a main phase")
    void cannotActivateAtInstantSpeed() {
        addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
