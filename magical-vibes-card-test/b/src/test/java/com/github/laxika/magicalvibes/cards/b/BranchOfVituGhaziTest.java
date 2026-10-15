package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BranchOfVituGhazi.class, Murder.class})
class BranchOfVituGhaziTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new BranchOfVituGhazi());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void turningFaceUpAddsPersistentManaOfChosenColor() {
        harness.setHand(player1, List.of(new BranchOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Vitu-Ghazi");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(branch));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void faceUpManaSurvivesStepTransitionButOtherManaDoesNot() {
        harness.setHand(player1, List.of(new BranchOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Vitu-Ghazi");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(branch));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLUE, 1);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    void persistentManaExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new BranchOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Vitu-Ghazi");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(branch));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.passUntil(player2, TurnStep.UPKEEP);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(pool.getPersistentMana(ManaColor.WHITE)).isZero();
    }

    @Test
    void canTapForManaImmediatelyAfterTurningFaceUp() {
        harness.setHand(player1, List.of(new BranchOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Vitu-Ghazi");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(branch));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(branch), 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(branch.isTapped()).isTrue();
    }

    @Test
    void disguiseWardCountersOpponentSpellWhenTheyCannotPay() {
        harness.setHand(player1, List.of(new BranchOfVituGhazi()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Vitu-Ghazi");
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, branch.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Branch of Vitu-Ghazi");
        harness.assertInGraveyard(player2, "Murder");
        assertThat(branch.isFaceDown()).isTrue();
    }
}
