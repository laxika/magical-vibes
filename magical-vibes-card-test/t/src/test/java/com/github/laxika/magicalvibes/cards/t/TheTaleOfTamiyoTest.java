package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTaleOfTamiyo.class, Shock.class, Forest.class, GrizzlyBears.class,
        Divination.class, TamiyoTheMoonSage.class, ChandraNalaar.class,
        LeylineOfTheVoid.class, MindSpring.class})
class TheTaleOfTamiyoTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield adds a lore counter and triggers chapter I")
    void enteringBattlefieldTriggersFirstChapter() {
        Forest land = new Forest();
        TheTaleOfTamiyo enchantment = new TheTaleOfTamiyo();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(land, enchantment, remaining));

        harness.castFromHand(player1, new TheTaleOfTamiyo(), "{2}{U}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "The Tale of Tamiyo").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Chapters I through III repeat milling and draw after a shared card type")
    void chaptersRepeatMillingAndDraw() {
        Shock first = new Shock();
        Shock second = new Shock();
        Forest drawn = new Forest();
        GrizzlyBears last = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, drawn, last));
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(first, second, last);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter IV targets instants, sorceries, and Tamiyo planeswalkers")
    void chapterIVFiltersGraveyards() {
        Card instant = new Shock();
        Card sorcery = new Divination();
        Card tamiyoPlaneswalker = new TamiyoTheMoonSage();
        Card otherPlaneswalker = new ChandraNalaar();
        Card land = new Forest();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, List.of(instant, sorcery, tamiyoPlaneswalker, otherPlaneswalker, land));
        harness.setGraveyard(player2, List.of(opponentInstant));
        addSagaWithLore(3);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                instant.getId(), sorcery.getId(), tamiyoPlaneswalker.getId());
    }

    @Test
    @DisplayName("Chapter IV exiles a selected card and offers its copy at normal cost")
    void chapterIVCastsSelectedCopy() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        addSagaWithLore(3);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each mill chapter stops when the two cards have different types")
    void eachMillChapterStopsWithoutSharedType(int initialLore) {
        Forest land = new Forest();
        TheTaleOfTamiyo enchantment = new TheTaleOfTamiyo();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(land, enchantment, remaining));
        addSagaWithLore(initialLore);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, enchantment);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each mill chapter repeats through multiple matching pairs")
    void eachMillChapterRepeatsMultipleTimes(int initialLore) {
        Forest first = new Forest();
        Forest second = new Forest();
        TheTaleOfTamiyo firstDraw = new TheTaleOfTamiyo();
        Forest third = new Forest();
        Forest fourth = new Forest();
        TheTaleOfTamiyo secondDraw = new TheTaleOfTamiyo();
        Forest lastLand = new Forest();
        TheTaleOfTamiyo lastEnchantment = new TheTaleOfTamiyo();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, firstDraw, third, fourth,
                secondDraw, lastLand, lastEnchantment));
        addSagaWithLore(initialLore);

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(first, second, third, fourth, lastLand, lastEnchantment);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mill chapters still draw and repeat when matching cards are exiled instead")
    void exiledMilledCardsStillCountForSharedType() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest drawn = new Forest();
        TheTaleOfTamiyo last = new TheTaleOfTamiyo();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, drawn, last));
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, last);
    }

    @Test
    @DisplayName("Chapter IV permits selecting no targets and then sacrifices the Saga")
    void chapterIVAllowsNoTargets() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        addSagaWithLore(3);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "The Tale of Tamiyo");
        harness.assertInGraveyard(player1, "The Tale of Tamiyo");
    }

    @Test
    @DisplayName("Chapter IV exiles all selected cards even when every copy is declined")
    void chapterIVAllowsDecliningAllCopies() {
        Shock shock = new Shock();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(shock, divination));
        addSagaWithLore(3);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), divination.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(shock, divination);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock, divination);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "The Tale of Tamiyo");
    }

    @Test
    @DisplayName("Chapter IV can cast a Tamiyo planeswalker copy for its normal mana cost")
    void chapterIVCastsTamiyoCopy() {
        TamiyoTheMoonSage tamiyo = new TamiyoTheMoonSage();
        harness.setGraveyard(player1, List.of(tamiyo));
        addSagaWithLore(3);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(tamiyo.getId()));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tamiyo, the Moon Sage");
        assertThat(findPermanent(player1, "Tamiyo, the Moon Sage").getCard().getId())
                .isNotEqualTo(tamiyo.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(tamiyo);
    }

    @Test
    @DisplayName("Chapter IV allows choosing a nonzero X when casting a copy")
    void chapterIVAllowsChoosingX() {
        MindSpring mindSpring = new MindSpring();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setGraveyard(player1, List.of(mindSpring));
        addSagaWithLore(3);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(mindSpring.getId()));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mindSpring);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTaleOfTamiyo());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
    }
}
