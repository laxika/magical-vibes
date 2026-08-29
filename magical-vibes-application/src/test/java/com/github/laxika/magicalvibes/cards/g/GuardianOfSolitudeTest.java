package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());

        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
