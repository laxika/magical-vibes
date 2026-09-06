package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberwildeAugur.class, GarrukWildspeaker.class, GrizzlyBears.class})
class EmberwildeAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 3 damage to a player during its controller's upkeep")
    void sacrificesAndDealsDamageToPlayer() {
        Permanent augur = addCreatureReady(player1, new EmberwildeAugur());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(augur);
        harness.assertInGraveyard(player1, "Emberwilde Augur");
    }

    @Test
    @DisplayName("Deals 3 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new EmberwildeAugur());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can only be activated during its controller's upkeep")
    void onlyActivatesDuringControllersUpkeep() {
        Permanent augur = addCreatureReady(player1, new EmberwildeAugur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(augur);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new EmberwildeAugur());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
