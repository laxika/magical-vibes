package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmortalObligation.class, GrizzlyBears.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class ImmortalObligationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature from an opponent's graveyard under its owner's control with a duty counter")
    void returnsCreatureUnderItsOwnersControlWithDutyCounter() {
        Card target = new GrizzlyBears();

        castAgainst(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId())
                        && permanent.getCounterCount(CounterType.DUTY) == 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature with a duty counter is goaded and cannot attack or block the controller's creatures")
    void dutyCreatureHasThePrintedRestrictions() {
        Permanent returned = castAgainst(new GrizzlyBears());
        returned.setSummoningSick(false);

        assertThat(gqs.isGoaded(gd, returned)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, returned, player1.getId())).isFalse();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(returned);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duty counters");
    }

    @Test
    @DisplayName("Cannot target a creature card in the controller's graveyard")
    void cannotTargetOwnGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ImmortalObligation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureInOpponentsGraveyard() {
        Card target = new ImmortalObligation();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ImmortalObligation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingTargetDoesNotReturnAnythingOrEstablishRestrictions() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ImmortalObligation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent unrelated = addCreatureReady(player2, new GrizzlyBears());
        unrelated.setCounterCount(CounterType.DUTY, 1);
        assertThat(gqs.isGoaded(gd, unrelated)).isFalse();
        assertThat(als.canAttackDefender(gd, unrelated, player1.getId())).isTrue();
    }

    @Test
    void restrictionsEndWhenLastDutyCounterIsRemoved() {
        Permanent returned = castAgainst(new GrizzlyBears());
        returned.setSummoningSick(false);
        returned.setCounterCount(CounterType.DUTY, 0);
        harness.runStateBasedActions();

        assertThat(gqs.isGoaded(gd, returned)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isZero();
        assertThat(als.canAttackDefender(gd, returned, player1.getId())).isTrue();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(returned),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    void replacingDutyCounterDoesNotRestartExpiredRestrictions() {
        Permanent returned = castAgainst(new GrizzlyBears());
        returned.setSummoningSick(false);
        returned.setCounterCount(CounterType.DUTY, 0);
        harness.runStateBasedActions();
        returned.setCounterCount(CounterType.DUTY, 1);
        harness.runStateBasedActions();

        assertThat(gqs.isGoaded(gd, returned)).isFalse();
        assertThat(als.canAttackDefender(gd, returned, player1.getId())).isTrue();
    }

    @Test
    void dutyCounterOnAnotherCreatureDoesNotApplyThisSpellsRestrictions() {
        castAgainst(new GrizzlyBears());
        Permanent unrelated = addCreatureReady(player2, new GrizzlyBears());
        unrelated.setCounterCount(CounterType.DUTY, 1);

        assertThat(gqs.isGoaded(gd, unrelated)).isFalse();
        assertThat(als.canAttackDefender(gd, unrelated, player1.getId())).isTrue();
    }

    @Test
    void restrictionsPersistThroughSpellControllersNextTurn() {
        Permanent returned = castAgainst(new GrizzlyBears());
        advanceToUpkeep(player2);
        advanceToUpkeep(player1);

        assertThat(gqs.isGoaded(gd, returned)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isEqualTo(1);
        assertThat(als.canAttackDefender(gd, returned, player1.getId())).isFalse();
    }

    @Test
    void eachCastingProtectsItsOwnControllerAndOnlyItsReturnedCreature() {
        Permanent firstReturned = castAgainst(new GrizzlyBears());
        Card secondTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(secondTarget));
        harness.setHand(player2, List.of(new ImmortalObligation()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, secondTarget.getId());
        Permanent secondReturned = findPermanent(player1, "Grizzly Bears");
        firstReturned.setSummoningSick(false);
        secondReturned.setSummoningSick(false);

        assertThat(als.canAttackDefender(gd, firstReturned, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, secondReturned, player2.getId())).isFalse();
    }

    @Test
    void cannotAttackBattleControlledBySpellController() {
        Permanent returned = castAgainst(new GrizzlyBears());
        returned.setSummoningSick(false);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        assertThat(als.canAttackDefender(gd, returned, battle.getId())).isFalse();
    }

    private Permanent castAgainst(Card target) {
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ImmortalObligation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
    }
}
