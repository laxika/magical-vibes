package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GoblinCratermaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CorsairsOfUmbar.class, GrizzlyBears.class, GoblinCratermaker.class, BoggartShenanigans.class})
class CorsairsOfUmbarTest extends BaseCardTest {

    @Test
    @DisplayName("Amasses Orcs 3 after dealing combat damage without an Army")
    void amassesOrcsWithoutAnArmy() {
        Permanent corsairs = addReadyCorsairs();
        corsairs.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getCard().getSubtypes())
                .containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
    }

    @Test
    @DisplayName("Amasses Orcs 3 on an existing Army")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent corsairs = addReadyCorsairs();
        corsairs.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ORC);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Makes a Pirate unable to be blocked until end of turn")
    void makesPirateUnblockableUntilEndOfTurn() {
        Permanent corsairs = addReadyCorsairs();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, corsairs.getId());
        harness.passBothPriorities();

        assertThat(corsairs.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(corsairs.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature without a Goblin, Orc, or Pirate subtype")
    void cannotTargetCreatureWithoutMatchingSubtype() {
        Permanent corsairs = addReadyCorsairs();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin, Orc, or Pirate");
        assertThat(corsairs.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target an opposing Goblin while the source is tapped and summoning sick")
    void makesOpposingGoblinUnblockableWithoutTapCost() {
        Permanent corsairs = harness.addToBattlefieldAndReturn(player1, new CorsairsOfUmbar());
        corsairs.setTapped(true);
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinCratermaker());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.isCantBeBlocked()).isTrue();
        assertThat(corsairs.isCantBeBlocked()).isFalse();
        assertThat(corsairs.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can make the amassed Orc Army unblockable")
    void makesOrcArmyUnblockable() {
        Permanent corsairs = addReadyCorsairs();
        corsairs.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        Permanent army = findPermanent(player1, "Orc Army");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, army.getId());
        harness.passBothPriorities();

        assertThat(army.isCantBeBlocked()).isTrue();
        assertThat(corsairs.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Amass lets the controller choose one of multiple Armies")
    void choosesOneOfMultipleArmies() {
        Permanent corsairs = addReadyCorsairs();
        Permanent firstArmy = harness.addToBattlefieldAndReturn(player1, new CorsairsOfUmbar());
        Permanent secondArmy = harness.addToBattlefieldAndReturn(player1, new CorsairsOfUmbar());
        firstArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        secondArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        corsairs.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondArmy.getId()));

        assertThat(firstArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ORC);
        assertThat(secondArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondArmy.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ORC);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("An opposing Army does not prevent creating your own Orc Army")
    void ignoresOpposingArmyWhenAmassing() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new CorsairsOfUmbar());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent corsairs = addReadyCorsairs();
        corsairs.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ORC);
    }


    @Test
    @DisplayName("Can target a noncreature Goblin permanent")
    void canTargetNoncreatureGoblin() {
        addReadyCorsairs();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.isCantBeBlocked()).isTrue();
    }

    private Permanent addReadyCorsairs() {
        return addCreatureReady(player1, new CorsairsOfUmbar());
    }
}
