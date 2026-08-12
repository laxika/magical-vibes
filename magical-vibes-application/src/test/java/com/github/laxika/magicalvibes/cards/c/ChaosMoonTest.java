package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChaosMoonTest extends BaseCardTest {

    /** Both parity branches sit on the upkeep slot, so two triggers go on the stack every upkeep. */
    private void resolveUpkeepTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Odd permanent count gives red creatures +1/+1 and leaves other colors alone")
    void oddCountBoostsRedCreatures() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        resolveUpkeepTriggers();

        assertThat(findPermanent(player1, "Hill Giant").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Hill Giant").getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Even permanent count gives red creatures -1/-1")
    void evenCountShrinksRedCreatures() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToUpkeep(player1);
        resolveUpkeepTriggers();

        assertThat(findPermanent(player1, "Hill Giant").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Hill Giant").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The upkeep ability uses the permanent count from its single resolution")
    void parityIsEvaluatedOnceForUpkeepAbility() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mountain));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hill Giant").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Hill Giant").getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Odd permanent count: tapping a Mountain for mana adds an additional {R}")
    void oddCountAddsExtraRedFromMountain() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToUpkeep(player1);
        resolveUpkeepTriggers();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional {R} is symmetric — an opponent tapping a Mountain gets it too")
    void extraRedIsSymmetric() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new Mountain());

        advanceToUpkeep(player1);
        resolveUpkeepTriggers();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Even permanent count: a Mountain produces colorless mana instead of red")
    void evenCountMakesMountainsProduceColorless() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        resolveUpkeepTriggers();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The mana clauses last only until end of turn")
    void manaClauseWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        resolveUpkeepTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mountain = findPermanent(player1, "Mountain");
        mountain.untap();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
