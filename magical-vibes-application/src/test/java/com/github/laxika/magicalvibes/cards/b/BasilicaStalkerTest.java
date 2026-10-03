package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasilicaStalker.class, Murder.class})
class BasilicaStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player gains 1 life and surveils 1")
    void combatDamageGainsLifeAndSurveils() {
        Permanent stalker = addCreatureReady(player1, new BasilicaStalker());
        Card topCard = new BasilicaStalker();
        Card keptCard = new BasilicaStalker();
        harness.setLibrary(player1, List.of(topCard, keptCard));
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(stalker)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Disguise casts Basilica Stalker face down")
    void disguiseCastsFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BasilicaStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Basilica Stalker").isFaceDown()).isTrue();
    }

    @Test
    void surveilCanKeepTheTopCard() {
        addCreatureReady(player1, new BasilicaStalker());
        Card topCard = new BasilicaStalker();
        Card nextCard = new BasilicaStalker();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 21);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void emptyLibraryDoesNotPreventLifeGain() {
        addCreatureReady(player1, new BasilicaStalker());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void faceDownCombatDamageDoesNotTriggerPrintedAbility() {
        Permanent stalker = castFaceDownStalker();
        stalker.setSummoningSick(false);
        Card topCard = new BasilicaStalker();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void disguiseWardCountersAnUnpaidOpponentsSpell() {
        Permanent stalker = castFaceDownStalker();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, stalker.getId());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player2, false);
        }
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Basilica Stalker");
        harness.assertInGraveyard(player2, "Murder");
    }

    @Test
    void turningFaceUpForDisguiseCostRestoresCombatTrigger() {
        Permanent stalker = castFaceDownStalker();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, 0);
        assertThat(stalker.isFaceDown()).isFalse();
        stalker.setSummoningSick(false);
        Card topCard = new BasilicaStalker();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    private Permanent castFaceDownStalker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BasilicaStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Basilica Stalker");
    }
}
