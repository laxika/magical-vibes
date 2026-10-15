package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealockMonster.class, Forest.class, Island.class})
class SealockMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity adds three counters and makes the chosen land an Island in addition to its types")
    void monstrosityMakesTargetLandAnIsland() {
        Permanent sealockMonster = addReadySealockMonster();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(sealockMonster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(sealockMonster.isMonstrous()).isTrue();
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.ISLAND);
        assertThat(harness.getGameQueryService().effectiveLandTypes(gd, forest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The monstrosity trigger is skipped when no land can be targeted")
    void monstrosityTriggerSkipsWithoutLand() {
        Permanent sealockMonster = addReadySealockMonster();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sealockMonster.isMonstrous()).isTrue();
        assertThat(sealockMonster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sealock Monster cannot attack without an Island under the defending player's control")
    void cannotAttackWithoutIsland() {
        addReadySealockMonster();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sealock Monster can attack when the defending player controls an Island")
    void canAttackWithDefendersIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());
        addReadySealockMonster();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Monstrosity may be activated again after the creature is monstrous, but does nothing")
    void monstrousCreatureCanActivateMonstrosityAgain() {
        Permanent monster = addReadySealockMonster();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(monster.isMonstrous()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An Island controlled by the attacker does not allow Sealock Monster to attack")
    void attackersOwnIslandDoesNotAllowAttack() {
        addReadySealockMonster();
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land made an Island by monstrosity allows an attack by the monstrous creature")
    void grantedIslandAllowsAttack() {
        addReadySealockMonster();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The monstrosity trigger can target the controller's own land")
    void monstrosityCanTargetOwnLand() {
        addReadySealockMonster();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().effectiveLandTypes(gd, forest))
                .contains(CardSubtype.FOREST, CardSubtype.ISLAND);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySealockMonster() {
        return addCreatureReady(player1, new SealockMonster());
    }
}
