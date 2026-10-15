package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
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

@CardUsed({HauntedOne.class, WalkingCorpse.class, GrizzlyBears.class, LightningBolt.class, Humility.class, ControlMagic.class})
class HauntedOneTest extends BaseCardTest {

    @Test
    @DisplayName("Commander creatures you own boost themselves and creatures sharing a type when tapped")
    void commanderTapBoostsSharingCreaturesAndGrantsUndying() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
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
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
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
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
        Permanent otherZombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        tapAndResolve(commander);
        harness.castInstant(player1, 0, otherZombie.getId());
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();

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

    @Test
    @DisplayName("An owned commander controlled by an opponent grants the bonus to that opponent's creatures")
    void opponentControllingOwnedCommanderControlsGrantedTrigger() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
        Permanent allyZombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, commander.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(commander);
        int allyPower = gqs.getEffectivePower(gd, allyZombie);
        int opponentPower = gqs.getEffectivePower(gd, opponentZombie);

        commander.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, commander));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(opponentPower + 2);
        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.UNDYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, commander, Keyword.UNDYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, allyZombie)).isEqualTo(allyPower);
        assertThat(gqs.hasKeyword(gd, allyZombie, Keyword.UNDYING)).isFalse();
    }

    @Test
    @DisplayName("A commander leaving before resolution still boosts creatures sharing its last known type")
    void usesLastKnownCreatureTypesAfterCommanderLeaves() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        int powerBefore = gqs.getEffectivePower(gd, zombie);

        commander.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, commander));
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToCommandZone(gd, commander));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(powerBefore + 2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.UNDYING)).isTrue();
    }

    @Test
    @DisplayName("Humility entering after Haunted One removes the commander's granted tap ability")
    void laterHumilityRemovesGrantedAbility() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
        harness.setHand(player1, List.of(new Humility()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        commander.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, commander));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping a noncommander does not grant any bonus")
    void noncommanderDoesNotTrigger() {
        harness.addToBattlefield(player1, new HauntedOne());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        int powerBefore = gqs.getEffectivePower(gd, zombie);

        zombie.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, zombie));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(powerBefore);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.UNDYING)).isFalse();
    }
}
