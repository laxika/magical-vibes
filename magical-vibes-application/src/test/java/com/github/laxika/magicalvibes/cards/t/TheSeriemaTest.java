package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSeriema.class, GrizzlyBears.class})
class TheSeriemaTest extends BaseCardTest {

    @Test
    void entersAndSearchesForLegendaryCreature() {
        Card legendaryCreature = card("Legendary creature", CardType.CREATURE);
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Card nonlegendaryCreature = card("Nonlegendary creature", CardType.CREATURE);
        Card legendaryArtifact = card("Legendary artifact", CardType.ARTIFACT);
        legendaryArtifact.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        setLibrary(nonlegendaryCreature, legendaryArtifact, legendaryCreature);
        harness.setHand(player1, List.of(new TheSeriema()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(legendaryCreature);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(legendaryCreature.getId()));
        assertThat(gameData.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(legendaryCreature.getId()));
    }

    @Test
    void stationUsesTappedCreaturePowerAndUnlocksTheSeriemaAtSevenCounters() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(seriema), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(seriema.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, seriema)).isFalse();

        seriema.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, seriema)).isTrue();
        assertThat(gqs.getEffectivePower(gd, seriema)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seriema)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, seriema, Keyword.FLYING)).isTrue();
    }

    @Test
    void grantsIndestructibleOnlyToOtherTappedLegendaryCreaturesYouControl() {
        harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        Card tappedLegendaryCard = card("Tapped legendary", CardType.CREATURE);
        tappedLegendaryCard.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent tappedLegendary = addCreatureReady(player1, tappedLegendaryCard);
        tappedLegendary.tap();
        Permanent untappedLegendary = addCreatureReady(player1, legendaryCreature("Untapped legendary"));
        Permanent tappedNonlegendary = addCreatureReady(player1, card("Tapped nonlegendary", CardType.CREATURE));
        tappedNonlegendary.tap();
        Permanent opponentLegendary = addCreatureReady(player2, legendaryCreature("Opponent legendary"));
        opponentLegendary.tap();

        assertThat(gqs.hasKeyword(gd, tappedLegendary, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, untappedLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, tappedNonlegendary, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void stationRequiresAnotherUntappedCreature() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seriema), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card legendaryCreature(String name) {
        Card card = card(name, CardType.CREATURE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }

    private Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void setLibrary(Card... cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
