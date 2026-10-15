package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfSolitude.class, DevotedRetainer.class, DesperateRitual.class, HarshDeceiver.class})
class GuardianOfSolitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell grants flying to the chosen creature")
    void arcaneSpellGrantsFlying() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        assertThat(retainer.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a Spirit spell grants flying to the chosen creature")
    void spiritSpellGrantsFlying() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        assertThat(retainer.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The triggered ability can target an opponent's creature")
    void spiritSpellCanTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent opponentRetainer = addCreatureReady(player2, new DevotedRetainer());

        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, opponentRetainer.getId());
        harness.passBothPriorities();

        assertThat(opponentRetainer.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(retainer.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger Guardian of Solitude")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());

        harness.castFromHand(player1, new DevotedRetainer(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Arcane spell does not trigger Guardian of Solitude")
    void opponentsArcaneSpellDoesNotTrigger() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfSolitude());

        harness.castFromHand(player2, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(guardian.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Guardian of Solitude can target itself")
    void guardianCanTargetItself() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfSolitude());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, guardian.getId());
        harness.passBothPriorities();

        assertThat(guardian.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting Guardian of Solitude does not trigger its own ability")
    void castingGuardianDoesNotTriggerItself() {
        harness.castFromHand(player1, new GuardianOfSolitude(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Guardian of Solitude");
        Permanent guardian = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(guardian.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
