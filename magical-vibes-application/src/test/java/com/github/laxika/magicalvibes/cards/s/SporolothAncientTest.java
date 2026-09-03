package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SporolothAncient.class, GrizzlyBears.class})
class SporolothAncientTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, puts a spore counter on itself")
    void upkeepTriggerAddsSporeCounter() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures you control can remove two spore counters to create a Saproling")
    void controlledCreatureUsesGrantedAbility() {
        harness.addToBattlefield(player1, new SporolothAncient());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.FUNGUS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(bears), 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(countSaprolings()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sporoloth Ancient can use the ability it grants")
    void sourceUsesGrantedAbility() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        ancient.setCounterCount(CounterType.FUNGUS, 2);

        harness.activateAbility(player1, battlefieldIndex(ancient), 0, null, null);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(countSaprolings()).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted ability requires two spore counters")
    void requiresTwoSporeCounters() {
        Permanent ancient = harness.addToBattlefieldAndReturn(player1, new SporolothAncient());
        ancient.setCounterCount(CounterType.FUNGUS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(ancient), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ancient.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures controlled by an opponent do not gain the ability")
    void doesNotGrantAbilityToOpponentCreatures() {
        harness.addToBattlefield(player1, new SporolothAncient());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        opponentBears.setCounterCount(CounterType.FUNGUS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private long countSaprolings() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SAPROLING))
                .count();
    }
}
