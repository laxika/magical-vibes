package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrawlingBarrens.class})
class CrawlingBarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Crawling Barrens produces colorless mana")
    void tappingProducesColorlessMana() {
        addReadyBarrens(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activation puts two +1/+1 counters on Crawling Barrens")
    void activationPutsCountersOnIt() {
        Permanent barrens = addReadyBarrens(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(barrens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, barrens)).isFalse();
    }

    @Test
    @DisplayName("Accepting the activation's choice animates Crawling Barrens until end of turn")
    void acceptingAnimationChoiceAnimatesIt() {
        Permanent barrens = addReadyBarrens(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(barrens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, barrens)).isTrue();
        assertThat(gqs.getEffectivePower(gd, barrens)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barrens)).isEqualTo(2);
        assertThat(barrens.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(barrens.getCard().hasType(CardType.LAND)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, barrens)).isFalse();
        assertThat(barrens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated animation keeps all accumulated counters")
    void repeatedAnimationKeepsAccumulatedCounters() {
        Permanent barrens = addReadyBarrens(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(barrens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, barrens)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barrens)).isEqualTo(4);
        assertThat(barrens.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining another animation does not end an existing animation")
    void decliningSecondAnimationKeepsItAnimated() {
        Permanent barrens = addReadyBarrens(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, barrens)).isTrue();
        assertThat(barrens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, barrens)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barrens)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped, newly entered Barrens can activate its counter ability")
    void tappedNewLandCanActivateCounterAbility() {
        Permanent barrens = harness.addToBattlefieldAndReturn(player1, new CrawlingBarrens());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(barrens.isTapped()).isTrue();
        assertThat(barrens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, barrens)).isTrue();
        assertThat(gqs.getEffectivePower(gd, barrens)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barrens)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
    private Permanent addReadyBarrens(Player player) {
        Permanent barrens = harness.addToBattlefieldAndReturn(player, new CrawlingBarrens());
        barrens.setSummoningSick(false);
        return barrens;
    }
}
