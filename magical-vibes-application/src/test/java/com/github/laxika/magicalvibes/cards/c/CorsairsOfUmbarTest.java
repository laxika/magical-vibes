package com.github.laxika.magicalvibes.cards.c;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorsairsOfUmbar.class, GrizzlyBears.class})
class CorsairsOfUmbarTest extends BaseCardTest {

    @Test
    @DisplayName("Amasses Orcs 3 after dealing combat damage without an Army")
    void amassesOrcsWithoutAnArmy() {
        Permanent corsairs = addReadyCorsairs();
        corsairs.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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

    private Permanent addReadyCorsairs() {
        Permanent corsairs = harness.addToBattlefieldAndReturn(player1, new CorsairsOfUmbar());
        corsairs.setSummoningSick(false);
        return corsairs;
    }
}
