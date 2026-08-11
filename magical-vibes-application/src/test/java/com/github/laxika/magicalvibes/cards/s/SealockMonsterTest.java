package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(forest.getCard().getSubtypes()).contains(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("The monstrosity trigger is skipped when no land can be targeted")
    void monstrosityTriggerSkipsWithoutLand() {
        Permanent sealockMonster = addReadySealockMonster();
        harness.addToBattlefield(player2, new GrizzlyBears());
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sealock Monster can attack when the defending player controls an Island")
    void canAttackWithDefendersIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());
        addReadySealockMonster();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    private Permanent addReadySealockMonster() {
        Permanent permanent = new Permanent(new SealockMonster());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
