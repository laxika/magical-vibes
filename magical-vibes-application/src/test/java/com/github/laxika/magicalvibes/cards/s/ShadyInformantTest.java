package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadyInformant.class, Murder.class})
class ShadyInformantTest extends BaseCardTest {

    @Test
    void disguiseCastsFaceDownAndTurnsFaceUp() {
        Permanent informant = castFaceDown();
        assertThat(informant.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(informant));

        assertThat(informant.isFaceDown()).isFalse();
    }

    @Test
    void deathTriggerDealsTwoDamageToChosenPlayer() {
        Permanent informant = harness.addToBattlefieldAndReturn(player1, new ShadyInformant());
        int lifeBefore = gd.getLife(player2.getId());

        killWithMurder(informant.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void deathTriggerCanKillAnotherCreature() {
        Permanent informant = harness.addToBattlefieldAndReturn(player1, new ShadyInformant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShadyInformant());

        killWithMurder(informant.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Shady Informant");
        harness.handlePermanentChosen(player2, player1.getId());
        int lifeBefore = gd.getLife(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void dyingFaceDownDoesNotTriggerPrintedAbility() {
        Permanent informant = castFaceDown();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, informant.getId());

        harness.assertNotOnBattlefield(player1, "Shady Informant");
        harness.assertInGraveyard(player1, "Shady Informant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void disguiseWardCountersOpponentRemovalWithoutExtraMana() {
        Permanent informant = castFaceDown();
        killWithMurder(informant.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(informant);
        harness.assertInGraveyard(player2, "Murder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingDisguiseWardAllowsRemovalWithoutADeathTrigger() {
        Permanent informant = castFaceDown();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        killWithMurder(informant.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Shady Informant");
        harness.assertInGraveyard(player1, "Shady Informant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void redManaCanPayBothHybridSymbolsAndRestoreDeathTrigger() {
        Permanent informant = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(informant));
        assertThat(informant.isFaceDown()).isFalse();

        killWithMurder(informant.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        int lifeBefore = gd.getLife(player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void mixedBlackAndRedManaCanPayHybridSymbols() {
        Permanent informant = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(informant));

        assertThat(informant.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShadyInformant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Shady Informant");
    }

    private void killWithMurder(java.util.UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
