package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EloiseNephaliaSleuth.class, Clue.class, GrizzlyBears.class, IzzetCluestone.class, Shock.class})
class EloiseNephaliaSleuthTest extends BaseCardTest {

    @Test
    void anotherCreatureDeathInvestigates() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        killWithShock(player2, bears);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificingTokenTriggersSurveil() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Clue clueCard = new Clue();
        clueCard.setToken(true);
        Permanent clue = harness.addToBattlefieldAndReturn(player1, clueCard);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void sacrificingNonTokenDoesNotTriggerSurveil() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Permanent cluestone = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cluestone), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void opponentsCreatureDeathDoesNotInvestigate() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        killWithShock(player1, bears);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void ownDeathDoesNotInvestigate() {
        Permanent eloise = addCreatureReady(player1, new EloiseNephaliaSleuth());

        killWithShock(player2, eloise);
        killWithShock(player2, eloise);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eloise.getCard());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void destroyedCreatureTokenInvestigatesWithoutSurveilling() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        GrizzlyBears tokenCopy = new GrizzlyBears();
        tokenCopy.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCopy);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        killWithShock(player2, token);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void investigatedClueSurveilsBeforeDrawing() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        killWithShock(player2, bears);
        resolveAllTriggers();
        Permanent clue = findPermanent(player1, "Clue");
        Card topCard = new GrizzlyBears();
        Card nextCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void keepingSurveilledCardDrawsThatCard() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Clue clueCard = new Clue();
        clueCard.setToken(true);
        Permanent clue = harness.addToBattlefieldAndReturn(player1, clueCard);
        Card topCard = new GrizzlyBears();
        Card nextCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void opponentsTokenSacrificeDoesNotSurveil() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Clue clueCard = new Clue();
        clueCard.setToken(true);
        Permanent clue = harness.addToBattlefieldAndReturn(player2, clueCard);
        Card ownTopCard = new GrizzlyBears();
        Card opponentsTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentsTopCard));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentsTopCard);
    }

    @Test
    void sacrificingNonClueTokenSurveils() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        IzzetCluestone tokenCopy = new IzzetCluestone();
        tokenCopy.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        Card topCard = new GrizzlyBears();
        Card nextCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    void emptyLibrarySurveilDoesNotAskForAChoice() {
        addCreatureReady(player1, new EloiseNephaliaSleuth());
        Clue clueCard = new Clue();
        clueCard.setToken(true);
        Permanent clue = harness.addToBattlefieldAndReturn(player1, clueCard);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("library is empty (Eloise, Nephalia Sleuth surveil)")).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
