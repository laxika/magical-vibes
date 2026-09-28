package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CityOfDeath.class)
class CityOfDeathTest extends BaseCardTest {

    @Test
    void chapterOneCreatesTreasure() {
        harness.setHand(player1, List.of(new CityOfDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
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
