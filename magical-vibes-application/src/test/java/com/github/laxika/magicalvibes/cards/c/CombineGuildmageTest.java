package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CombineGuildmage.class, GrizzlyBears.class})
class CombineGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives creatures entering this turn an additional +1/+1 counter")
    void creaturesEnterWithAdditionalCounter() {
        addCreatureReady(player1, new CombineGuildmage());
        activateEnterWithCounterAbility();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability moves exactly one +1/+1 counter")
    void movesOnePlusOneCounter() {
        addCreatureReady(player1, new CombineGuildmage());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        source.setCounterCount(CounterType.CHARGE, 1);

        activateMoveCounterAbility(List.of(source.getId(), destination.getId()));

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can target only creatures you control")
    void moveAbilityRejectsOpponentCreature() {
        addCreatureReady(player1, new CombineGuildmage());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void enterCounterEffectStacksAndSurvivesSourceLeaving() {
        Permanent guildmage = addCreatureReady(player1, new CombineGuildmage());
        activateEnterWithCounterAbility();
        guildmage.setTapped(false);
        activateEnterWithCounterAbility();
        gd.playerBattlefields.get(player1.getId()).remove(guildmage);

        harness.setHand(player1, List.of(new CombineGuildmage(), new CombineGuildmage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Combine Guildmage"))
                .hasSize(2)
                .allSatisfy(creature -> assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }

    @Test
    void enterCounterEffectDoesNotAffectOpponentCreaturesOrExistingCreatures() {
        Permanent guildmage = addCreatureReady(player1, new CombineGuildmage());
        activateEnterWithCounterAbility();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CombineGuildmage()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(guildmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Combine Guildmage")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void moveAbilityDoesNothingWhenSourceHasNoPlusOnePlusOneCounter() {
        addCreatureReady(player1, new CombineGuildmage());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CombineGuildmage());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new CombineGuildmage());
        source.setCounterCount(CounterType.CHARGE, 1);

        activateMoveCounterAbility(List.of(source.getId(), destination.getId()));

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void moveAbilityRequiresDifferentCreatures() {
        addCreatureReady(player1, new CombineGuildmage());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CombineGuildmage());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void moveAbilityDoesNotRemoveCounterWhenDestinationBecomesIllegal() {
        addCreatureReady(player1, new CombineGuildmage());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CombineGuildmage());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new CombineGuildmage());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(destination);
        gd.playerBattlefields.get(player2.getId()).add(destination);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void activateEnterWithCounterAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void activateMoveCounterAbility(List<UUID> targets) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, targets);
        harness.passBothPriorities();
    }
}
