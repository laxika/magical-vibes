package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BriselaVoiceOfNightmares;
import com.github.laxika.magicalvibes.cards.b.BrunaTheFadingLight;
import com.github.laxika.magicalvibes.cards.r.RatchetBomb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiselaTheBrokenBlade.class, BrunaTheFadingLight.class,
        BriselaVoiceOfNightmares.class, RatchetBomb.class})
class GiselaTheBrokenBladeTest extends BaseCardTest {

    @Test
    void unblockedAttackDealsDamageAndGainsLife() {
        var gisela = addCreatureReady(player1, new GiselaTheBrokenBlade());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(gisela.isTapped()).isTrue();
    }

    @Test
    void tokenCopyOfGiselaIsExiledButCannotMeld() {
        GiselaTheBrokenBlade token = new GiselaTheBrokenBlade();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        BrunaTheFadingLight bruna = new BrunaTheFadingLight();
        harness.addToBattlefield(player1, bruna);

        advanceToControllerEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brisela, Voice of Nightmares");
        harness.assertNotOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertNotOnBattlefield(player1, "Bruna, the Fading Light");
        assertThat(gd.exiledCards).anyMatch(c -> c.card().getId().equals(bruna.getId()));
    }

    @Test
    void doesNotTriggerWhenBrunaIsOwnedByOpponent() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());
        BrunaTheFadingLight bruna = new BrunaTheFadingLight();
        bruna.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, bruna);

        advanceToControllerEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
    }

    @Test
    void doesNotTriggerWhenGiselaIsOwnedByOpponent() {
        GiselaTheBrokenBlade gisela = new GiselaTheBrokenBlade();
        gisela.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, gisela);
        harness.addToBattlefield(player1, new BrunaTheFadingLight());

        advanceToControllerEndStep();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());
        harness.addToBattlefield(player1, new BrunaTheFadingLight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
    }

    @Test
    void doesNotMeldWhenGiselaLeavesBeforeResolution() {
        var gisela = harness.addToBattlefieldAndReturn(player1, new GiselaTheBrokenBlade());
        harness.addToBattlefield(player1, new BrunaTheFadingLight());
        advanceToControllerEndStep();
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gisela));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gisela, the Broken Blade");
        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertNotOnBattlefield(player1, "Brisela, Voice of Nightmares");
    }

    @Test
    void meldedBriselaIsNotDestroyedByZeroCounterRatchetBomb() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());
        harness.addToBattlefield(player1, new BrunaTheFadingLight());
        advanceToControllerEndStep();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Brisela, Voice of Nightmares");
        harness.addToBattlefield(player2, new RatchetBomb());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brisela, Voice of Nightmares");
        harness.assertNotInGraveyard(player1, "Gisela, the Broken Blade");
        harness.assertNotInGraveyard(player1, "Bruna, the Fading Light");
    }

    @Test
    void meldedBriselaIsDestroyedByElevenCounterRatchetBomb() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());
        harness.addToBattlefield(player1, new BrunaTheFadingLight());
        advanceToControllerEndStep();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Brisela, Voice of Nightmares");
        var bomb = harness.addToBattlefieldAndReturn(player2, new RatchetBomb());
        bomb.setCounterCount(CounterType.CHARGE, 11);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brisela, Voice of Nightmares");
        harness.assertInGraveyard(player1, "Gisela, the Broken Blade");
        harness.assertInGraveyard(player1, "Bruna, the Fading Light");
    }

    @Test
    void tokenCopyOfBrunaIsExiledButCannotMeld() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());
        BrunaTheFadingLight token = new BrunaTheFadingLight();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        advanceToControllerEndStep();
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brisela, Voice of Nightmares");
        harness.assertNotOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertNotOnBattlefield(player1, "Bruna, the Fading Light");
        assertThat(gd.exiledCards).anyMatch(c -> c.card().getName().equals("Gisela, the Broken Blade"));
    }

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
        Permanent brisela = findPermanent(player1, "Brisela, Voice of Nightmares");
        assertThat(brisela.getMeldComponentCards()).hasSize(2);
        assertThat(brisela.getCard()).isInstanceOf(BriselaVoiceOfNightmares.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Brisela enters under her own printing, not Gisela's")
    void briselaEntersUnderHerOwnPrinting() {
        Card gisela = new GiselaTheBrokenBlade();
        gisela.setSetCode("INR");
        gisela.setCollectorNumber("24");
        harness.addToBattlefield(player1, gisela);
        harness.addToBattlefield(player1, namedBruna());

        advanceToControllerEndStep();
        harness.passBothPriorities();

        Card brisela = findPermanent(player1, "Brisela, Voice of Nightmares").getCard();
        assertThat(brisela.getSetCode()).isEqualTo("INR");
        assertThat(brisela.getCollectorNumber()).isEqualTo("14b");
    }

    @Test
    @DisplayName("End step does not trigger without Bruna")
    void doesNotTriggerWithoutBruna() {
        harness.addToBattlefield(player1, new GiselaTheBrokenBlade());

        advanceToControllerEndStep();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gisela, the Broken Blade");
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
        harness.assertNotOnBattlefield(player1, "Brisela, Voice of Nightmares");
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

        Permanent brisela = findPermanent(player1, "Brisela, Voice of Nightmares");

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, brisela);

        harness.assertNotOnBattlefield(player1, "Brisela, Voice of Nightmares");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(giselaCard, brunaCard);
    }

    private static Card namedBruna() {
        return new BrunaTheFadingLight();
    }

    private void advanceToControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
