package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfTheWorldsoul.class, GrizzlyBears.class})
class SongOfTheWorldsoulTest extends BaseCardTest {

    @Test
    void populatesWhenControllerCastsASpell() {
        harness.addToBattlefield(player1, song());
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Soldier Token"));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
        assertThat(findPermanents(player1, "Soldier Token"))
                .anyMatch(permanent -> !permanent.getId().equals(token.getId()));
    }

    @Test
    void doesNothingWhenControllerHasNoCreatureTokens() {
        harness.addToBattlefield(player1, song());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void doesNotTriggerForAnOpponentsSpell() {
        harness.addToBattlefield(player1, song());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesExactlyOneControlledCreatureTokenDuringResolution() {
        harness.addToBattlefield(player1, song());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, creatureToken("Soldier Token"));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, bearToken());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, creatureToken("Opponent Token"));
        Card artifactToken = creatureToken("Artifact Token");
        artifactToken.setType(CardType.ARTIFACT);
        harness.addToBattlefield(player1, artifactToken);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(soldier.getId(), bear.getId());

        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(findPermanents(player1, "Artifact Token")).hasSize(1);
        assertThat(findPermanents(player2, "Opponent Token")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canPopulateATokenThatEnteredAfterTheSpellWasCast() {
        harness.addToBattlefield(player1, song());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.addToBattlefield(player1, bearToken());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void doesNothingIfTheLastCreatureTokenLeavesBeforeResolution() {
        harness.addToBattlefield(player1, song());
        Permanent token = harness.addToBattlefieldAndReturn(player1, bearToken());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(token);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void queuedPopulateStillResolvesAfterSongLeavesTheBattlefield() {
        Permanent song = harness.addToBattlefieldAndReturn(player1, song());
        harness.addToBattlefield(player1, bearToken());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(song);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void noncreatureSpellTriggersPopulateBeforeThatSpellResolves() {
        harness.addToBattlefield(player1, song());
        harness.addToBattlefield(player1, bearToken());

        harness.castFromHand(player1, song(), "{4}{W}{W}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Song of the Worldsoul")).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Song of the Worldsoul")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingSongDoesNotTriggerItsOwnAbility() {
        harness.addToBattlefield(player1, bearToken());

        harness.castFromHand(player1, song(), "{4}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertOnBattlefield(player1, "Song of the Worldsoul");
    }

    private static GrizzlyBears bearToken() {
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        return token;
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }

    private static SongOfTheWorldsoul song() {
        SongOfTheWorldsoul song = new SongOfTheWorldsoul();
        song.setName("Song of the Worldsoul");
        return song;
    }
}
