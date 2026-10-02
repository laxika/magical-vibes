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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AinokBondKin.class, AlpineGrizzly.class})
class AinokBondKinTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Ainok Bond-Kin and taps it")
    void outlastPutsCounterAndTaps() {
        Permanent bondKin = addBondKinReady(player1);
        prepareOutlast();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bondKin.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(bondKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addBondKinReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A creature you control with a +1/+1 counter has first strike")
    void counteredOwnCreatureHasFirstStrike() {
        Permanent bondKin = addBondKinReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        bondKin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bondKin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creatures without a +1/+1 counter and opponents' creatures do not gain first strike")
    void onlyCounteredOwnCreaturesHaveFirstStrike() {
        addBondKinReady(player1);
        Permanent uncountered = addCreatureReady(player1, new AlpineGrizzly());
        Permanent opponentCreature = addCreatureReady(player2, new AlpineGrizzly());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike is lost when the +1/+1 counter is removed")
    void firstStrikeEndsWhenCounterIsRemoved() {
        addBondKinReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void outlastDoesNotAddCounterUntilResolution() {
        Permanent bondKin = addBondKinReady(player1);
        prepareOutlast();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bondKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, bondKin, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();
        assertThat(bondKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bondKin, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void outlastCannotBeActivatedWhileTapped() {
        Permanent bondKin = addBondKinReady(player1);
        bondKin.setTapped(true);
        prepareOutlast();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(bondKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void outlastCannotBeActivatedWithSummoningSickness() {
        Permanent bondKin = harness.addToBattlefieldAndReturn(player1, new AinokBondKin());
        bondKin.setSummoningSick(true);
        prepareOutlast();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(bondKin.isTapped()).isFalse();
    }

    @Test
    void outlastCannotBeActivatedWithAnAbilityOnTheStack() {
        addBondKinReady(player1);
        Permanent second = addBondKinReady(player1);
        prepareOutlast();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(second.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void outlastRequiresWhiteMana() {
        Permanent bondKin = addBondKinReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bondKin.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void outlastCanBeActivatedInPostcombatMainPhase() {
        Permanent bondKin = addBondKinReady(player1);
        prepareOutlast();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bondKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void firstStrikeEndsWhenBondKinLeavesBattlefield() {
        Permanent bondKin = addBondKinReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(bondKin);
        harness.setGraveyard(player1, java.util.List.of(bondKin.getCard()));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void otherCounterTypesDoNotGrantFirstStrike() {
        addBondKinReady(player1);
        Permanent creature = addCreatureReady(player1, new AlpineGrizzly());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void prepareOutlast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addBondKinReady(Player player) {
        return addCreatureReady(player, new AinokBondKin());
    }
}
