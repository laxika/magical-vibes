package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MattMurdockJusticeSeeker.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class MattMurdockJusticeSeekerTest extends BaseCardTest {

    @Test
    void payingAtBeginningOfCombatPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningPaymentDoesNotPutCounter() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyCreaturesYouControlCanBeTargeted() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    void creaturesYouControlWithCountersHaveWard() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        putCounterOn(protectedCreature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unprotectedCreature.getId());
        harness.passBothPriorities();

        assertThat(unprotectedCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void mattCanPutCounterOnHimself() {
        Permanent matt = harness.addToBattlefieldAndReturn(player1, new MattMurdockJusticeSeeker());

        putCounterOn(matt);

        assertThat(matt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent matt = harness.addToBattlefieldAndReturn(player1, new MattMurdockJusticeSeeker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(matt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentCanRespondToReflexiveCounterTrigger() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void payingWardAllowsSpellToResolve() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void nonPlusOneCounterAlsoGrantsWardAgainstAbilities() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownSpellDoesNotTriggerGrantedWard() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardStopsBeingGrantedWhenMattLeavesBattlefield() {
        Permanent matt = harness.addToBattlefieldAndReturn(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, matt.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Matt Murdock, Justice Seeker");
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void acceptingWithoutManaDoesNotPutCounter() {
        Permanent matt = harness.addToBattlefieldAndReturn(player1, new MattMurdockJusticeSeeker());

        beginCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(matt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningAffordableWardCountersSpell() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void removingLastCounterRemovesWard() {
        harness.addToBattlefield(player1, new MattMurdockJusticeSeeker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();
        assertThat(target.getMarkedDamage()).isZero();

        target.setCounterCount(CounterType.CHARGE, 0);
        Permanent pyromancer = gd.playerBattlefields.get(player2.getId()).getFirst();
        pyromancer.untap();
        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void putCounterOn(Permanent target) {
        beginCombat();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
