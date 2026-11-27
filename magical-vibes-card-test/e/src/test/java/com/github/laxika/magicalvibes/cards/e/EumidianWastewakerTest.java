package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.ChoiceContext;
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

@CardUsed({EumidianWastewaker.class, Forest.class, GrizzlyBears.class, LeylineOfTheVoid.class,
        AshayaSoulOfTheWild.class})
class EumidianWastewakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking counts lands discarded or sacrificed and draws that many cards")
    void attackCountsLandsPutIntoGraveyards() {
        Permanent wastewaker = addCreatureReady(player1, new EumidianWastewaker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1,
                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wastewaker);
    }

    @Test
    @DisplayName("Encore creates a hasty attacking token and sacrifices it at the next end step")
    void encoreCreatesAndSacrificesToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EumidianWastewaker()));
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eumidian Wastewaker");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        assertThat(token.isAttacking()).isFalse();

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        declareAttackers(List.of(tokenIndex));

        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());

        harness.passBothPriorities();
        harness.handleListChoice(player1,
                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eumidian Wastewaker")).isEmpty();
    }

    @Test
    @CardUsed({com.github.laxika.magicalvibes.cards.r.RestInPeace.class})
    @DisplayName("A sacrificed land that is exiled instead of going to a graveyard draws no card (ruling)")
    void sacrificedLandExiledInsteadDrawsNothing() {
        addCreatureReady(player1, new EumidianWastewaker());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.r.RestInPeace());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both players choose before anything is discarded or sacrificed, then all happen together (ruling)")
    void bothPlayersChooseBeforeAnyoneActs() {
        addCreatureReady(player1, new EumidianWastewaker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.ColorChoice defendingChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(defendingChoice).isNotNull();
        assertThat(defendingChoice.playerId()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        harness.assertNotInGraveyard(player1, "Forest");

        harness.handleListChoice(player2, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("A chosen permanent stays on the battlefield until the defending player has chosen too")
    void chosenPermanentIsSacrificedAfterEveryChoice() {
        addCreatureReady(player1, new EumidianWastewaker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Forest").getId());

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        harness.handleListChoice(player2, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("A discarded land that a replacement exiles instead draws no card (ruling)")
    void discardedLandExiledInsteadDrawsNothing() {
        addCreatureReady(player1, new EumidianWastewaker());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player2, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A sacrificed creature that is a land only on the battlefield draws no card (ruling)")
    void sacrificedLandOnlyOnBattlefieldDrawsNothing() {
        addCreatureReady(player1, new EumidianWastewaker());
        harness.addToBattlefield(player2, new AshayaSoulOfTheWild());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Ashaya, Soul of the Wild");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
