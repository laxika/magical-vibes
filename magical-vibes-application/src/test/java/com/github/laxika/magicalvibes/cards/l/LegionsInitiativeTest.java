package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.ViashinoFirstblade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegionsInitiative.class, EliteVanguard.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, ViashinoFirstblade.class})
class LegionsInitiativeTest extends BaseCardTest {

    @Test
    @DisplayName("Red creatures you control get +1/+0")
    void boostsOwnRedCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());

        Permanent giant = findPermanent(player1, "Hill Giant");

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("White creatures you control get +0/+1")
    void boostsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new LegionsInitiative());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Neither boost applies to a green creature or to an opponent's red creature")
    void leavesOtherCreaturesAlone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentGiant = findPermanent(player2, "Hill Giant");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating exiles itself and every creature you control, sparing lands and the opponent's creatures")
    void activationExilesYourCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new LegionsInitiative());

        activateInitiative();

        harness.assertNotOnBattlefield(player1, "Legion's Initiative");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Hill Giant", "Elite Vanguard", "Legion's Initiative");
    }

    @Test
    @DisplayName("Exiled creatures all return together at the beginning of the next combat with haste")
    void creaturesReturnAtNextCombatWithHaste() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new LegionsInitiative());

        activateInitiative();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        advanceToBeginningOfCombat();

        Permanent giant = findPermanent(player1, "Hill Giant");
        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        assertThat(giant).isNotNull();
        assertThat(vanguard).isNotNull();
        assertThat(giant.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(vanguard.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(giant.isTapped()).isFalse();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("The exiled enchantment stays exiled — only the creatures come back")
    void enchantmentDoesNotReturn() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());

        activateInitiative();
        advanceToBeginningOfCombat();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Legion's Initiative");
    }

    @Test
    @DisplayName("Returned creatures are new objects, so the anthem is gone and they are unboosted")
    void returnedCreaturesLoseTheAnthem() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());

        activateInitiative();
        advanceToBeginningOfCombat();

        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("A red and white creature receives both boosts")
    void boostsRedAndWhiteCreature() {
        Permanent firstblade = harness.addToBattlefieldAndReturn(player1, new ViashinoFirstblade());
        harness.addToBattlefield(player1, new LegionsInitiative());

        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstblade)).isEqualTo(3);
    }

    @Test
    @DisplayName("The haste granted on return expires at end of turn")
    void returnedCreatureLosesHasteAtEndOfTurn() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());

        activateInitiative();
        advanceToBeginningOfCombat();
        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures with different owners return simultaneously from one delayed trigger")
    void differentlyOwnedCreaturesReturnTogether() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());
        harness.addToBattlefield(player1, new LegionsInitiative());

        activateInitiative();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> harness.passBothPriorities());

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Elite Vanguard");
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling the enchantment is paid before the creature-exile ability resolves")
    void enchantmentIsExiledAsActivationCost() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Legion's Initiative")),
                null, null);

        harness.assertNotOnBattlefield(player1, "Legion's Initiative");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The next combat returns creatures even during an opponent's turn")
    void creaturesReturnDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new LegionsInitiative());
        harness.forceActivePlayer(player2);

        activateInitiative();
        advanceToBeginningOfCombat();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Hill Giant"), Keyword.HASTE)).isTrue();
    }

    private void activateInitiative() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int index = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Legion's Initiative"));
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat() {
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);
    }
}
