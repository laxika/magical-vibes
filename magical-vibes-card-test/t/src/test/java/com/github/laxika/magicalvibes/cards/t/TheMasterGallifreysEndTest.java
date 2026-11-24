package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.AdricMathematicalGenius;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterGallifreysEnd.class, CopperMyr.class, GrizzlyBears.class, Shock.class, AdricMathematicalGenius.class})
class TheMasterGallifreysEndTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent may choose to lose 4 life")
    void opponentCanChooseLifeLoss() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();

        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertNotInGraveyard(player1, "Copper Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> "Copper Myr".equals(card.getName()));
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
    }

    @Test
    @DisplayName("The opponent may choose to give the controller a token copy of the exiled card")
    void opponentCanChooseTokenCopy() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.CREATE_TOKEN_OPTION);

        Permanent copy = findPermanent(player1, "Copper Myr");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    @DisplayName("Declining leaves the artifact creature in the graveyard")
    void decliningLeavesTheArtifactCreatureInTheGraveyard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Copper Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    @DisplayName("A nonartifact creature's death doesn't trigger")
    void ignoresNonArtifactCreatureDeaths() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player2, bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A face-down artifact creature triggers and the copy is of its face-up card")
    void faceDownCybermanTriggersAndCopiesTheFaceUpCard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent cyberman = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        cyberman.setFaceDown(2, 2, Set.of(CardType.ARTIFACT, CardType.CREATURE));

        killWithShock(player2, cyberman);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.CREATE_TOKEN_OPTION);

        Permanent copy = findPermanent(player1, "Adric, Mathematical Genius");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.isFaceDown()).isFalse();
        harness.assertNotInGraveyard(player1, "Adric, Mathematical Genius");
    }

    @Test
    @DisplayName("A face-down creature that isn't an artifact doesn't trigger")
    void faceDownMyrWithoutArtifactTypeDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        killWithShock(player2, creature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Copper Myr");
    }

    @Test
    @DisplayName("The Master triggers on its own death while it is an artifact creature")
    void masterTriggersForItsOwnDeathWhenItIsAnArtifactCreature() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterGallifreysEnd());
        PermanentGrants.cardType(gd, master, CardType.ARTIFACT);
        master.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();

        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertNotInGraveyard(player1, "The Master, Gallifrey's End");
    }

    @Test
    @DisplayName("An opponent's artifact creature dying doesn't trigger")
    void opposingArtifactCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new CopperMyr());

        killWithShock(player1, myr);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Copper Myr");
    }

    @Test
    @DisplayName("The token copy dying doesn't trigger again")
    void tokenCopyDeathDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        killWithShock(player2, myr);
        acceptExile();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.CREATE_TOKEN_OPTION);

        killWithShock(player2, findPermanent(player1, "Copper Myr"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only opponents' life totals decide who faces the choice")
    void controllerHavingMoreLifeDoesNotPreventTheOpponentBeingChosen() {
        harness.setLife(player1, 40);
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.LOSE_LIFE_OPTION);

        harness.assertLife(player1, 40);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("No villainous choice when the dying card has left the graveyard")
    void noVillainousChoiceWhenTheDyingCardIsNoLongerInTheGraveyard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        killWithShock(player2, myr);
        harness.setGraveyard(player1, List.of());

        acceptExile();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    @DisplayName("The Master sees an artifact creature that dies with it")
    void masterSeesAnotherArtifactCreatureDieSimultaneously() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        master.setMarkedDamage(3);
        myr.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEnd.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "The Master, Gallifrey's End");
        harness.assertNotInGraveyard(player1, "Copper Myr");
    }

    @Test
    @DisplayName("The opponent with the most life among the opponents is chosen unasked")
    void opponentWithMostLifeIsChosenUnasked() {
        Player player3 = addOpponent();
        harness.setLife(player2, 12);
        harness.setLife(player3, 15);
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player1, myr);
        acceptExile();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player3.getId());
        harness.handleListChoice(player3, TheMasterGallifreysEnd.LOSE_LIFE_OPTION);

        harness.assertLife(player3, 11);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Between opponents tied for the most life, the controller chooses")
    void controllerChoosesBetweenTiedOpponents() {
        Player player3 = addOpponent();
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player1, myr);
        acceptExile();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), player3.getId());
        harness.handlePermanentChosen(player1, player3.getId());
        harness.handleListChoice(player3, TheMasterGallifreysEnd.LOSE_LIFE_OPTION);

        harness.assertLife(player3, 16);
        harness.assertLife(player2, 20);
    }

    private Player addOpponent() {
        UUID playerId = UUID.randomUUID();
        Player opponent = new Player(playerId, "Charlie");
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(playerId, "Charlie");
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerCommandZones.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), playerId, "Charlie");
        return opponent;
    }

    private void acceptExile() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
    }

    private void assertVillainousChoice() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                TheMasterGallifreysEnd.LOSE_LIFE_OPTION,
                TheMasterGallifreysEnd.CREATE_TOKEN_OPTION);
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }
}
