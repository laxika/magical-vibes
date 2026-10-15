package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidalFlats.class, GrizzlyBears.class, WindDrake.class})
class TidalFlatsTest extends BaseCardTest {

    @Test
    @DisplayName("Unpaid nonflying attackers give first strike to their blockers")
    void unpaidAttackerGrantsFirstStrikeToBlockers() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Paying prevents first strike from being granted")
    void paymentPreventsFirstStrikeGrant() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activateTidalFlats();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Flying attackers do not receive payment choices")
    void flyingAttackersAreIgnored() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent groundAttacker = addAttacker(player2, new GrizzlyBears());
        Permanent flyingAttacker = addAttacker(player2, new WindDrake());
        Permanent groundBlocker = addBlocker(player1, groundAttacker);
        Permanent flyingBlocker = addBlocker(player1, new WindDrake(), flyingAttacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(groundBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(flyingBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Each nonflying attacker is handled by an independent payment choice")
    void handlesEachAttackerIndependently() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent unpaidAttacker = addAttacker(player2, new GrizzlyBears());
        Permanent paidAttacker = addAttacker(player2, new GrizzlyBears());
        Permanent unpaidBlockerOne = addBlocker(player1, unpaidAttacker);
        Permanent unpaidBlockerTwo = addBlocker(player1, unpaidAttacker);
        Permanent paidBlocker = addBlocker(player1, paidAttacker);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activateTidalFlats();

        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(unpaidBlockerOne.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(unpaidBlockerTwo.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(paidBlocker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An unblocked nonflying attacker still gets a payment choice")
    void unblockedAttackerStillGetsPaymentChoice() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures blocking the unpaid attacker gain first strike")
    void onlyBlockersOfUnpaidAttackerGainFirstStrike() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(blocker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bystander.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Resolving before blockers are declared does not grant first strike to later blockers")
    void laterBlockersDoNotGainFirstStrike() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent futureBlocker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();
        harness.handleMayAbilityChosen(player2, false);

        futureBlocker.setBlocking(true);
        futureBlocker.addBlockingTargetId(attacker.getId());

        assertThat(gqs.hasKeyword(gd, futureBlocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Colored mana can pay the attacker's generic payment")
    void coloredManaPaysGenericCost() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        activateTidalFlats();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The ability still grants first strike after Tidal Flats leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TidalFlats());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent blocker = addBlocker(player1, attacker);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("With no attacking creatures the ability finishes without payment choices")
    void noAttackersProducesNoPaymentChoices() {
        harness.addToBattlefield(player1, new TidalFlats());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        activateTidalFlats();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, bystander, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void activateTidalFlats() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Permanent attacker) {
        return addBlocker(owner, new GrizzlyBears(), attacker);
    }

    private Permanent addBlocker(Player owner, Card card, Permanent attacker) {
        Permanent blocker = addCreatureReady(owner, card);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }
}
