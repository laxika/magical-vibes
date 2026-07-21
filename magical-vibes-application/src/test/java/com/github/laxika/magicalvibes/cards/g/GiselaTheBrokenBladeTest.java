package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BriselaVoiceOfNightmares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiselaTheBrokenBladeTest extends BaseCardTest {

    @Test
    @DisplayName("End step melds with owned Bruna into Brisela")
    void meldsWithBrunaAtEndStep() {
        Permanent gisela = harness.addToBattlefieldAndReturn(player1, new GiselaTheBrokenBlade());
        Permanent bruna = harness.addToBattlefieldAndReturn(player1, namedBruna());

        advanceToControllerEndStep();
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(gisela.getId()) || p.getId().equals(bruna.getId()));
        Permanent brisela = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Brisela, Voice of Nightmares"))
                .findFirst()
                .orElseThrow();
        assertThat(brisela.getMeldComponentCards()).hasSize(2);
        assertThat(brisela.getCard()).isInstanceOf(BriselaVoiceOfNightmares.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("End step does not trigger without Bruna")
    void doesNotTriggerWithoutBruna() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());

        advanceToControllerEndStep();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gisela, the Broken Blade"));
    }

    @Test
    @DisplayName("End step does not trigger when only opponent controls Bruna")
    void doesNotTriggerWithOpponentBruna() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());
        harness.addToBattlefield(player2, namedBruna());

        advanceToControllerEndStep();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Meld fizzles if Bruna leaves before resolution")
    void fizzlesIfBrunaLeavesBeforeResolution() {
        Permanent gisela = harness.addToBattlefieldAndReturn(player1, new GiselaTheBrokenBlade());
        Permanent bruna = harness.addToBattlefieldAndReturn(player1, namedBruna());

        advanceToControllerEndStep();
        assertThat(gd.stack).isNotEmpty();

        // Remove Bruna before the meld resolves
        gd.playerBattlefields.get(player1.getId()).remove(bruna);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(gisela.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Brisela, Voice of Nightmares"));
    }

    @Test
    @DisplayName("Destroying Brisela puts both meld components into the graveyard")
    void destroyingBriselaPutsBothComponentsInGraveyard() {
        Permanent gisela = harness.addToBattlefieldAndReturn(player1, new GiselaTheBrokenBlade());
        Permanent bruna = harness.addToBattlefieldAndReturn(player1, namedBruna());
        Card giselaCard = gisela.getOriginalCard();
        Card brunaCard = bruna.getOriginalCard();

        advanceToControllerEndStep();
        harness.passBothPriorities();

        Permanent brisela = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Brisela, Voice of Nightmares"))
                .findFirst()
                .orElseThrow();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, brisela);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Brisela, Voice of Nightmares"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(giselaCard, brunaCard);
    }

    private static Card namedBruna() {
        Card bruna = new Card();
        bruna.setName("Bruna, the Fading Light");
        bruna.setType(CardType.CREATURE);
        bruna.setPower(5);
        bruna.setToughness(7);
        return bruna;
    }

    private void advanceToControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
    }
}
