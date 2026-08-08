package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void activateInitiative() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int index = indexOnBattlefield(player1, "Legion's Initiative");
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Beginning of combat holds no priority with an empty stack, so the engine runs straight on
        // to declare attackers — the delayed return has already fired by then.
        assertThat(gd.currentStep.isCombatPhase()).isTrue();
    }

    private int indexOnBattlefield(com.github.laxika.magicalvibes.model.Player player, String name) {
        var battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException(name + " is not on the battlefield");
    }
}
