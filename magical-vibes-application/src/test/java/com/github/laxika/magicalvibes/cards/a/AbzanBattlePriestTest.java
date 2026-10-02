package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbzanBattlePriest.class, AlpineGrizzly.class})
class AbzanBattlePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Abzan Battle Priest and taps it")
    void outlastPutsCounterAndTaps() {
        Permanent priest = addPriestReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(priest.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addPriestReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A creature you control with a +1/+1 counter has lifelink")
    void counteredOwnCreatureHasLifelink() {
        Permanent priest = addPriestReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        priest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, priest, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain lifelink")
    void onlyCounteredOwnCreaturesHaveLifelink() {
        addPriestReady(player1);
        Permanent uncountered = addCreatureReady(player1, new AlpineGrizzly());
        Permanent opponentCreature = addCreatureReady(player2, new AlpineGrizzly());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink is lost when the +1/+1 counter is removed")
    void lifelinkEndsWhenCounterIsRemoved() {
        addPriestReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Outlast cannot be activated by a summoning-sick priest")
    void outlastRequiresNoSummoningSickness() {
        harness.addToBattlefield(player1, new AbzanBattlePriest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Outlast cannot be activated while the priest is tapped")
    void outlastRequiresUntappedPriest() {
        Permanent priest = addPriestReady(player1);
        priest.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Outlast requires white mana")
    void outlastRequiresWhiteMana() {
        Permanent priest = addPriestReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(priest.isTapped()).isFalse();
        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Outlast cannot be activated during an opponent's main phase")
    void outlastRequiresOwnTurn() {
        addPriestReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Outlast cannot be activated while another ability is on the stack")
    void outlastRequiresEmptyStack() {
        addPriestReady(player1);
        addPriestReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Countered creatures lose lifelink when the priest leaves the battlefield")
    void lifelinkEndsWhenPriestLeaves() {
        Permanent priest = addPriestReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(priest);
        gd.playerGraveyards.get(player1.getId()).add(priest.getCard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The priest grants lifelink for counters placed after it enters")
    void lifelinkBeginsWhenCounterIsAdded() {
        Permanent priest = addPriestReady(player1);
        assertThat(gqs.hasKeyword(gd, priest, Keyword.LIFELINK)).isFalse();
        priest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, priest, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("A countered creature's combat damage gains life for its controller")
    void grantedLifelinkGainsLifeInCombat() {
        addPriestReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    private Permanent addPriestReady(Player player) {
        return addCreatureReady(player, new AbzanBattlePriest());
    }
}
