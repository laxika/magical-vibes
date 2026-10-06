package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.b.BlessedLight;
import com.github.laxika.magicalvibes.cards.k.KnightOfMalice;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayamiFirstOfTheFallen.class, SerraAngel.class, ZetalpaPrimalDawn.class,
        KnightOfMalice.class, BlessedLight.class})
class RayamiFirstOfTheFallenTest extends BaseCardTest {

    @Test
    void exilesNontokenCreaturesWithBloodCountersAndGainsTheirKeywords() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angel));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(angel.getCard());
        assertThat(gd.exiledCardsWithBloodCounters).contains(angel.getCard().getId());
        assertThat(gd.getCardsExiledByPermanent(rayami.getId())).contains(angel.getCard());
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player2, "Serra Angel");
    }

    @Test
    @CardUsed(Card.class)
    void doesNotReplaceTokenCreatureDeaths() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Card tokenCard = new Card();
        tokenCard.setName("Flying Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setColor(CardColor.WHITE);
        tokenCard.setSubtypes(List.of(CardSubtype.ANGEL));
        tokenCard.setKeywords(Set.of(Keyword.FLYING));
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(tokenCard);
        assertThat(gd.exiledCardsWithBloodCounters).doesNotContain(tokenCard.getId());
        assertThat(gd.getCardsExiledByPermanent(rayami.getId())).doesNotContain(tokenCard);
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.FLYING)).isFalse();
    }

    @Test
    void gainsKeywordsFromCardsExiledByAnOpponentsRayami() {
        Permanent firstRayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angel));
        Permanent secondRayami = harness.addToBattlefieldAndReturn(player2, new RayamiFirstOfTheFallen());

        assertThat(gd.getCardsExiledByPermanent(firstRayami.getId())).contains(angel.getCard());
        assertThat(gqs.hasKeyword(gd, secondRayami, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondRayami, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void laterRayamiGainsKeywordsFromBloodCountersLeftByAnEarlierRayami() {
        Permanent firstRayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, angel);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstRayami);
        });
        Permanent laterRayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(angel.getCard());
        assertThat(gqs.hasKeyword(gd, laterRayami, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterRayami, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void losingAbilitiesDisablesTheDeathReplacement() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        rayami.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angel));

        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(angel.getCard());
        assertThat(gd.exiledCardsWithBloodCounters).doesNotContain(angel.getCard().getId());
    }

    @Test
    void losingAbilitiesAlsoAllowsRayamiItselfToDie() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        rayami.setLosesAllAbilitiesUntilEndOfTurn(true);
        rayami.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Rayami, First of the Fallen");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rayami.getCard());
    }

    @Test
    void exilesItselfAndOtherCreaturesDyingSimultaneously() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        rayami.setMarkedDamage(4);
        angel.setMarkedDamage(4);

        harness.runStateBasedActions();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rayami.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(angel.getCard());
        assertThat(gd.exiledCardsWithBloodCounters).contains(rayami.getCard().getId(), angel.getCard().getId());
        harness.assertNotInGraveyard(player1, "Rayami, First of the Fallen");
        harness.assertNotInGraveyard(player2, "Serra Angel");
    }

    @Test
    void gainsMultipleKeywordsFromASacrificedIndestructibleCreature() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        Permanent zetalpa = harness.addToBattlefieldAndReturn(player1, new ZetalpaPrimalDawn());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, zetalpa));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zetalpa.getCard());
        for (Keyword keyword : Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE, Keyword.VIGILANCE,
                Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE)) {
            assertThat(gqs.hasKeyword(gd, rayami, keyword)).isTrue();
        }
        harness.assertNotInGraveyard(player1, "Zetalpa, Primal Dawn");
    }

    @Test
    void inheritsHexproofFromWhiteFromAnExiledKnightOfMalice() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player2, new RayamiFirstOfTheFallen());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfMalice());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, knight));
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new BlessedLight()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThat(gqs.hasKeyword(gd, rayami, Keyword.FIRST_STRIKE)).isTrue();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, rayami.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has hexproof from white");
    }

    @Test
    void doesNotGainKeywordsFromAnExiledCreatureWithoutABloodCounter() {
        Permanent rayami = harness.addToBattlefieldAndReturn(player1, new RayamiFirstOfTheFallen());
        harness.setExile(player2, List.of(new SerraAngel()));

        assertThat(gqs.hasKeyword(gd, rayami, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, rayami, Keyword.VIGILANCE)).isFalse();
    }
}
