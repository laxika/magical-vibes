package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellsword;
import com.github.laxika.magicalvibes.cards.e.ExpeditionSkulker;
import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.g.GnarlidColony;
import com.github.laxika.magicalvibes.cards.k.KorCelebrant;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchpriestOfIona.class, CliffhavenSellsword.class, ExpeditionSkulker.class,
        ExpeditionDiviner.class, GnarlidColony.class, KorCelebrant.class, StoneworkPackbeast.class})
class ArchpriestOfIonaTest extends BaseCardTest {

    @Test
    void partyPowerUsesGrantedSubtypesAndAssignsEachCreatureOnlyOnce() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player1, new StoneworkPackbeast());

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(2);

        harness.addToBattlefield(player1, new CliffhavenSellsword());
        harness.addToBattlefield(player1, new ExpeditionDiviner());

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(4);
    }

    @Test
    @DisplayName("Has power equal to party size and boosts a target with a full party")
    void fullPartyBoostsTarget() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        addFullParty();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidColony());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger without a full party")
    void doesNotBoostWithoutFullParty() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new ExpeditionSkulker());
        harness.addToBattlefield(player1, new CliffhavenSellsword());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidColony());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A creature with multiple party types fills only one role")
    void oneCreatureCannotFillTwoPartyRoles() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new CliffhavenSellsword());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidColony());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The temporary boost and flying wear off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new ArchpriestOfIona());
        addFullParty();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidColony());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void losingPartyMemberBeforeResolutionPreventsBothEffects() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player1, new ExpeditionSkulker());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellsword());
        harness.addToBattlefield(player1, new ExpeditionDiviner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidColony());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, warrior));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void canTargetItselfWithFullParty() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        addFullParty();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, archpriest.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, archpriest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, archpriest, Keyword.FLYING)).isTrue();
    }

    @Test
    void fullPartyDoesNotTriggerOnOpponentsTurn() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        addFullParty();

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, archpriest, Keyword.FLYING)).isFalse();
    }

    @Test
    void opposingPartyMembersDoNotIncreasePowerOrEnableTrigger() {
        Permanent archpriest = harness.addToBattlefieldAndReturn(player1, new ArchpriestOfIona());
        harness.addToBattlefield(player2, new ExpeditionSkulker());
        harness.addToBattlefield(player2, new CliffhavenSellsword());
        harness.addToBattlefield(player2, new ExpeditionDiviner());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, archpriest)).isEqualTo(1);
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new ExpeditionSkulker());
        harness.addToBattlefield(player1, new CliffhavenSellsword());
        harness.addToBattlefield(player1, new ExpeditionDiviner());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

}
