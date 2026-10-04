package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnigmaEidolon.class, AssaultZeppelid.class})
class EnigmaEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Enigma Eidolon mills three cards from the target player's library")
    void sacrificeAbilityMillsTargetPlayer() {
        harness.addToBattlefield(player1, new EnigmaEidolon());
        harness.setLibrary(player2, List.of(
                new EnigmaEidolon(), new EnigmaEidolon(), new EnigmaEidolon(), new EnigmaEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Enigma Eidolon");
    }

    @Test
    @DisplayName("Sacrificing Enigma Eidolon can target its controller")
    void sacrificeAbilityCanTargetItsController() {
        harness.addToBattlefield(player1, new EnigmaEidolon());
        harness.setLibrary(player1, List.of(
                new EnigmaEidolon(), new EnigmaEidolon(), new EnigmaEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Enigma Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        EnigmaEidolon eidolon = new EnigmaEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        harness.castFromHand(player1, new AssaultZeppelid(), "{2}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
    }

    @Test
    @DisplayName("Declining the multicolored spell trigger keeps Enigma Eidolon in the graveyard")
    void decliningReturnKeepsEidolonInGraveyard() {
        EnigmaEidolon eidolon = new EnigmaEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        harness.castFromHand(player1, new AssaultZeppelid(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Enigma Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        EnigmaEidolon eidolon = new EnigmaEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new EnigmaEidolon(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A sacrificed Eidolon can return when its owner casts a multicolored spell")
    void sacrificedEidolonReturnsToHand() {
        EnigmaEidolon eidolon = new EnigmaEidolon();
        harness.addToBattlefield(player1, eidolon);
        harness.setLibrary(player2, List.of(new EnigmaEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Enigma Eidolon");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        harness.castFromHand(player1, new AssaultZeppelid(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
    }

    @Test
    @DisplayName("An Eidolon on the battlefield does not trigger when a multicolored spell is cast")
    void battlefieldEidolonDoesNotTriggerReturn() {
        harness.addToBattlefield(player1, new EnigmaEidolon());

        harness.castFromHand(player1, new AssaultZeppelid(), "{2}{G}{U}");

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Enigma Eidolon");
    }

    @Test
    @DisplayName("An opponent's multicolored spell does not trigger Enigma Eidolon's graveyard ability")
    void opponentsMulticoloredSpellDoesNotTriggerReturn() {
        EnigmaEidolon eidolon = new EnigmaEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new AssaultZeppelid(), "{2}{G}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }
}
