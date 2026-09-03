package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldeviSentry.class, Forest.class, GrizzlyBears.class, Threaten.class})
class SoldeviSentryTest extends BaseCardTest {

    private Permanent addSentryReady() {
        addCreatureReady(player1, new SoldeviSentry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        return findPermanent(player1, "Soldevi Sentry");
    }

    /** Puts the Sentry in front of a 2/2 attacker so combat damage forces the regeneration. */
    private void blockGrizzlyBears(Permanent sentry) {
        blockGrizzlyBears(sentry, player2);
    }

    private void blockGrizzlyBears(Permanent sentry, Player attackingPlayer) {
        sentry.setBlocking(true);
        sentry.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(attackingPlayer, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(attackingPlayer);
    }

    @Test
    @DisplayName("Activating the ability only grants a shield — no draw is offered yet")
    void activationAloneOffersNoDraw() {
        addSentryReady();
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sentry = findPermanent(player1, "Soldevi Sentry");
        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Spending the shield regenerates the Sentry and offers the opponent a card")
    void regeneratingOffersOpponentDraw() {
        addSentryReady();
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blockGrizzlyBears(findPermanent(player1, "Soldevi Sentry"));

        harness.assertOnBattlefield(player1, "Soldevi Sentry");
        Permanent regenerated = findPermanent(player1, "Soldevi Sentry");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
        assertThat(regenerated.getMarkedDamage()).isZero();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }

    @Test
    @DisplayName("The opponent may decline the draw")
    void opponentMayDecline() {
        addSentryReady();
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        blockGrizzlyBears(findPermanent(player1, "Soldevi Sentry"));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A shield from another source regenerates without offering a draw")
    void foreignShieldOffersNoDraw() {
        Permanent sentry = addSentryReady();
        sentry.setRegenerationShield(1);
        harness.setLibrary(player2, List.of(new Forest()));

        blockGrizzlyBears(findPermanent(player1, "Soldevi Sentry"));

        harness.assertOnBattlefield(player1, "Soldevi Sentry");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The opponent chosen on activation remains the draw recipient after control changes")
    void chosenOpponentRemainsDrawRecipientAfterControlChange() {
        Permanent sentry = addSentryReady();
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Threaten()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, sentry.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sentry);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sentry);
        blockGrizzlyBears(sentry, player1);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice drawChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(drawChoice).isNotNull();
        assertThat(drawChoice.playerId()).isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Without a shield the Sentry dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent sentry = addSentryReady();

        blockGrizzlyBears(sentry);

        harness.assertNotOnBattlefield(player1, "Soldevi Sentry");
        harness.assertInGraveyard(player1, "Soldevi Sentry");
    }
}
