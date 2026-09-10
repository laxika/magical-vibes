package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LinvalaShieldOfSeaGate.class, BoggartBrute.class, FaerieMiscreant.class,
        FountainOfYouth.class, FugitiveWizard.class, GrizzlyBears.class, Plains.class, SoulWarden.class})
class LinvalaShieldOfSeaGateTest extends BaseCardTest {

    @Test
    @DisplayName("With a full party, detains a target nonland permanent until your next turn")
    void fullPartyDetainsTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new LinvalaShieldOfSeaGate());
        addFullParty();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Linvala, Shield of Sea Gate");
    }

    @Test
    @DisplayName("Cannot target a land with the full-party trigger")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new LinvalaShieldOfSeaGate());
        addFullParty();
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Without a full party, the combat ability does not trigger")
    void doesNotTriggerWithoutFullParty() {
        harness.addToBattlefield(player1, new LinvalaShieldOfSeaGate());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player2, new FountainOfYouth());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sacrificing Linvala grants the chosen keyword to creatures you control")
    void sacrificeGrantsChosenKeyword() {
        addCreatureReady(player1, new LinvalaShieldOfSeaGate());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Indestructible");

        harness.assertInGraveyard(player1, "Linvala, Shield of Sea Gate");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The chosen keyword wears off at end of turn")
    void chosenKeywordWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new LinvalaShieldOfSeaGate());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Hexproof");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
