package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArbalestEngineers.class, GrizzlyBears.class, Island.class})
class ArbalestEngineersTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 1 damage to any target")
    void damageModeDealsDamageToPlayer() {
        cast(0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Counter mode puts a counter on a creature and grants trample and haste")
    void counterModeAddsCounterAndKeywords() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Powerstone mode creates a tapped Powerstone token")
    void powerstoneModeCreatesTappedPowerstone() {
        cast(2, null);
        resolveAllTriggers();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        Permanent powerstone = powerstones.getFirst();
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counter mode cannot target a noncreature permanent")
    void counterModeRejectsNoncreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        enterAndChooseMode("Put a +1/+1 counter on target creature. It gains trample and haste until end of turn");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void canChoosePowerstoneWhenEnteringWithoutBeingCast() {
        enterAndChooseMode("Create a tapped Powerstone token");
        resolveAllTriggers();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    void counterModeCanTargetTheEnteringEngineers() {
        Permanent engineers = harness.enterBattlefieldAndReturn(player1, new ArbalestEngineers());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1,
                "Put a +1/+1 counter on target creature. It gains trample and haste until end of turn");
        harness.handlePermanentChosen(player1, engineers.getId());
        resolveAllTriggers();

        assertThat(engineers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, engineers, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, engineers, Keyword.HASTE)).isTrue();
    }

    @Test
    void damageModeDamagesACreatureWithoutChoosingOtherModes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArbalestEngineers());
        cast(0, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    private void enterAndChooseMode(String mode) {
        harness.enterBattlefieldAndReturn(player1, new ArbalestEngineers());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, mode);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ArbalestEngineers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

}
