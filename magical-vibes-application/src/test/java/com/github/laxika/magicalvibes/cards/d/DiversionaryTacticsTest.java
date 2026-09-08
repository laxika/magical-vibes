package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiversionaryTactics.class, GrizzlyBears.class, Plains.class})
class DiversionaryTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two creatures you control as a cost and taps the target creature")
    void tapsTwoCreaturesAndTargetCreature() {
        harness.addToBattlefield(player1, new DiversionaryTactics());
        Permanent firstCostCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCostCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Diversionary Tactics"));
        harness.activateAbility(player1, tacticsIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(firstCostCreature.isTapped()).isTrue();
        assertThat(secondCostCreature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without two untapped creatures you control")
    void cannotActivateWithoutTwoUntappedControlledCreatures() {
        harness.addToBattlefield(player1, new DiversionaryTactics());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Diversionary Tactics"));
        assertThatThrownBy(() -> harness.activateAbility(player1, tacticsIndex, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new DiversionaryTactics());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        int tacticsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Diversionary Tactics"));
        assertThatThrownBy(() -> harness.activateAbility(player1, tacticsIndex, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
