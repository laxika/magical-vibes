package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskaiDevotee.class, LightningBolt.class})
class JeskaiDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell gives Jeskai Devotee +1/+1 until end of turn")
    void secondSpellBoostsUntilEndOfTurn() {
        Permanent devotee = addCreatureReady(player1, new JeskaiDevotee());
        int initialPower = devotee.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower + 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("The mana ability adds a chosen Jeskai color and can be activated only once each turn")
    void manaAbilityAddsChosenColorAndIsLimitedToOnceEachTurn() {
        harness.addToBattlefield(player1, new JeskaiDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "RED", "WHITE"})
    void manaAbilityResolvesImmediatelyWithoutTappingEvenWhenSummoningSick(ManaColor color) {
        Permanent devotee = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        devotee.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(devotee.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @Test
    void manaAbilityCanBeUsedAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new JeskaiDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void creatureSpellsCountIncludingDevoteeCastBeforeItEntered() {
        harness.setHand(player1, List.of(new JeskaiDevotee(), new JeskaiDevotee()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        int initialPower = first.getEffectivePower();
        int initialToughness = first.getEffectiveToughness();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(first.getEffectiveToughness()).isEqualTo(initialToughness + 1);
        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(second.getEffectivePower()).isEqualTo(initialPower);
        assertThat(second.getEffectiveToughness()).isEqualTo(initialToughness);
    }

    @Test
    void opponentsSecondSpellDoesNotTriggerFlurry() {
        Permanent devotee = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        int initialPower = devotee.getEffectivePower();
        int initialToughness = devotee.getEffectiveToughness();
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);
        assertThat(devotee.getEffectiveToughness()).isEqualTo(initialToughness);
    }

    @Test
    void flurryCountsControllersSpellsOnOpponentsTurnAndResetsEachTurn() {
        Permanent devotee = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        int initialPower = devotee.getEffectivePower();
        int initialToughness = devotee.getEffectiveToughness();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);
        assertThat(devotee.getEffectiveToughness()).isEqualTo(initialToughness);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);
        resolveAllTriggers();

        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower + 1);
        assertThat(devotee.getEffectiveToughness()).isEqualTo(initialToughness + 1);
    }

    @Test
    void manaAbilityCanActivateWhileTappedAndEachDevoteeHasItsOwnLimit() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        first.tap();
        harness.addToBattlefield(player1, new JeskaiDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(first.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
