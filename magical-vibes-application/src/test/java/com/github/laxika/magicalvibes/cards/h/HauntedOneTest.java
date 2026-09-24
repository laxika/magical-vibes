package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntedOne.class, WalkingCorpse.class, GrizzlyBears.class, LightningBolt.class})
class HauntedOneTest extends BaseCardTest {

    @Test
    @DisplayName("Commander creatures you own boost themselves and creatures sharing a type when tapped")
    void commanderTapBoostsSharingCreaturesAndGrantsUndying() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        commander.setCommander(true);
        Permanent otherZombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        int commanderPower = gqs.getEffectivePower(gd, commander);
        int zombiePower = gqs.getEffectivePower(gd, otherZombie);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int opponentPower = gqs.getEffectivePower(gd, opponentZombie);

        tapAndResolve(commander);

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(commanderPower + 2);
        assertThat(gqs.getEffectivePower(gd, otherZombie)).isEqualTo(zombiePower + 2);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.UNDYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherZombie, Keyword.UNDYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.UNDYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(opponentPower);
        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.UNDYING)).isFalse();
    }

    @Test
    @DisplayName("The boost and undying grant last only until end of turn")
    void grantEndsAtEndOfTurn() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        commander.setCommander(true);
        int powerBefore = gqs.getEffectivePower(gd, commander);

        tapAndResolve(commander);
        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(powerBefore + 2);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.UNDYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(powerBefore);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.UNDYING)).isFalse();
    }

    @Test
    @DisplayName("Granted undying returns a same-type creature with a +1/+1 counter")
    void grantedUndyingReturnsCreature() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        commander.setCommander(true);
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        tapAndResolve(commander);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Walking Corpse"));
        resolveUntilStackEmpty();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Walking Corpse"))
                .filter(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1)
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void resolveUntilStackEmpty() {
        for (int i = 0; i < 12 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
    }
}
