package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CityOfDeath.class})
class CityOfDeathTest extends BaseCardTest {

    @Test
    void chapterOneCreatesTreasure() {
        harness.castFromHand(player1, new CityOfDeath(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void laterChaptersCopyAChosenTokenYouControl() {
        harness.addToBattlefield(player1, new CityOfDeath());
        Permanent target = addToken(player1, "Clue Token", CardType.ARTIFACT, List.of(CardSubtype.CLUE));
        addToken(player1, "Other Token", CardType.CREATURE, List.of());

        Permanent saga = findPermanent(player1, "City of Death");
        saga.setCounterCount(CounterType.LORE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue Token")).hasSize(2);
    }

    @Test
    void chaptersOnlyTargetNonSagaTokensYouControl() {
        harness.addToBattlefield(player1, new CityOfDeath());
        Permanent ownToken = addToken(player1, "Own Token", CardType.ARTIFACT, List.of(CardSubtype.CLUE));
        Permanent ownSagaToken = addToken(player1, "Saga Token", CardType.ENCHANTMENT,
                List.of(CardSubtype.SAGA));
        Permanent ownPermanent = addPermanent(player1, "Own Permanent", CardType.CREATURE, List.of(), false);
        Permanent opponentToken = addToken(player2, "Opponent Token", CardType.CREATURE, List.of());

        Permanent saga = findPermanent(player1, "City of Death");
        saga.setCounterCount(CounterType.LORE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(ownToken.getId())
                .doesNotContain(ownSagaToken.getId(), ownPermanent.getId(), opponentToken.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6})
    void eachLaterChapterCopiesTreasureAndOnlyFinalChapterSacrificesSaga(int chapter) {
        harness.castFromHand(player1, new CityOfDeath(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent treasure = findPermanent(player1, "Treasure");
        treasure.tap();
        treasure.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        beginChapter(chapter);
        harness.handlePermanentChosen(player1, treasure.getId());
        harness.assertOnBattlefield(player1, "City of Death");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        Permanent copy = findPermanents(player1, "Treasure").stream()
                .filter(permanent -> !permanent.getId().equals(treasure.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        if (chapter == 6) {
            harness.assertNotOnBattlefield(player1, "City of Death");
            harness.assertInGraveyard(player1, "City of Death");
        } else {
            harness.assertOnBattlefield(player1, "City of Death");
        }
    }

    @Test
    void mandatoryCopyChapterDoesNotOfferSkippingTargetSelection() {
        harness.castFromHand(player1, new CityOfDeath(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        beginChapter(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPlayerIds()).isEmpty();
    }

    @Test
    void tokenNoLongerControlledByYouIsIllegalOnResolution() {
        harness.castFromHand(player1, new CityOfDeath(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent treasure = findPermanent(player1, "Treasure");

        beginChapter(2);
        harness.handlePermanentChosen(player1, treasure.getId());
        gd.playerBattlefields.get(player1.getId()).remove(treasure);
        gd.playerBattlefields.get(player2.getId()).add(treasure);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).containsExactly(treasure);
    }

    @Test
    void finalChapterWithNoLegalTokenStillSacrificesSaga() {
        harness.addToBattlefield(player1, new CityOfDeath());

        beginChapter(6);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "City of Death");
        harness.assertInGraveyard(player1, "City of Death");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void beginChapter(int chapter) {
        findPermanent(player1, "City of Death").setCounterCount(CounterType.LORE, chapter - 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player, String name,
                               CardType type, List<CardSubtype> subtypes) {
        return addPermanent(player, name, type, subtypes, true);
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player, String name,
                                   CardType type, List<CardSubtype> subtypes, boolean token) {
        Card card = new Card() {
        };
        card.setName(name);
        card.setType(type);
        card.setColor(CardColor.WHITE);
        card.setToken(token);
        card.setSubtypes(subtypes);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
