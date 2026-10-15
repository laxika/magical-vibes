package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MentalVapors.class, ArmoredTransport.class, TurnToFrog.class})
class MentalVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card and cipher can be declined")
    void targetDiscardsAndCipherDeclined() {
        harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player2, new ArrayList<>(List.of(new ArmoredTransport(), new MentalVapors())));
        harness.setHand(player1, List.of(new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Armored Transport");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Mental Vapors");
    }

    @Test
    @DisplayName("Target with empty hand discards nothing")
    void emptyHandDiscardsNothing() {
        harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.setHand(player1, List.of(new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Encoded copy makes the defending player discard again after combat damage")
    void cipherCopyOnCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player2, new ArrayList<>(List.of(new ArmoredTransport(), new MentalVapors(), new MentalVapors())));
        harness.setHand(player1, List.of(new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Mental Vapors"));
        harness.assertNotInGraveyard(player1, "Mental Vapors");

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // CR 601.2c — the cipher copy is cast, so its controller chooses its target anew.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new MentalVapors(), new ArmoredTransport(), new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Armored Transport");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingCipherWithoutACreatureDoesNotExileSpell() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Mental Vapors");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void combatDamageCopyCanBeDeclined() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player2, List.of(new ArmoredTransport(), new MentalVapors()));
        harness.setHand(player1, List.of(new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cipherCopyCanChooseItsControllerAndCannotEncodeAgain() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MentalVapors(), new MentalVapors(), new ArmoredTransport()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Armored Transport");
        harness.assertNotInGraveyard(player1, "Mental Vapors");
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void losingAllAbilitiesSuppressesCipherCombatDamageTrigger() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MentalVapors()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        int lifeBeforeCombat = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeCombat - 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
    }
}
