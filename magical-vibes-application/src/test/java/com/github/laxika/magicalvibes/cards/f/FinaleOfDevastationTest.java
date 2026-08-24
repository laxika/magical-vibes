package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinaleOfDevastation.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class FinaleOfDevastationTest extends BaseCardTest {

    @Test
    @DisplayName("Searches either zone for a creature with mana value at most X")
    void searchesLibraryAndGraveyardWithinX() {
        Card libraryBear = new GrizzlyBears();
        Card libraryAirElemental = new AirElemental();
        Card graveyardBear = new GrizzlyBears();
        Card graveyardAirElemental = new AirElemental();
        setLibrary(libraryBear, libraryAirElemental);
        harness.setGraveyard(player1, List.of(graveyardBear, graveyardAirElemental));

        castFinale(2);
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(libraryBear.getId(), graveyardBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardBear.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == graveyardBear);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).doesNotContain(graveyardBear);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(libraryBear, libraryAirElemental);
    }

    @Test
    @DisplayName("At X=10, gives all controlled creatures +X/+X and haste")
    void givesBonusAtTen() {
        Card libraryCreature = new LlanowarElves();
        setLibrary(libraryCreature);
        harness.addToBattlefield(player1, new GrizzlyBears());

        castFinale(10);
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(libraryCreature.getId()));

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == libraryCreature)
                .findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, bears)).isEqualTo(12);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, bears)).isEqualTo(12);
        assertThat(harness.getGameQueryService().hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, elves)).isEqualTo(11);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, elves)).isEqualTo(11);
        assertThat(harness.getGameQueryService().hasKeyword(gd, elves, Keyword.HASTE)).isTrue();
    }

    private void castFinale(int xValue) {
        harness.setHand(player1, List.of(new FinaleOfDevastation()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 2);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setLibrary(Card... cards) {
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
