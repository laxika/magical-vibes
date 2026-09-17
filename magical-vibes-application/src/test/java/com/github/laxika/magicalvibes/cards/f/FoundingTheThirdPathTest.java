package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundingTheThirdPath.class, Divination.class, Forest.class, Shock.class})
class FoundingTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I offers only a low-mana instant or sorcery and casts it for free")
    void chapterICastsLowManaInstantOrSorceryForFree() {
        Shock shock = new Shock();
        Divination tooExpensive = new Divination();
        FoundingTheThirdPath sagaCard = new FoundingTheThirdPath();
        harness.setHand(player1, List.of(sagaCard, shock, tooExpensive));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(tooExpensive);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Chapter II mills four cards from the chosen player")
    void chapterIIMillsTargetPlayer() {
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, new ArrayList<>(milled));
        Permanent saga = addSagaWithLore(1);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(milled);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III exiles a graveyard spell and offers its copy for its normal cost")
    void chapterIIIExilesAndCastsGraveyardCopy() {
        Shock shock = new Shock();
        Forest invalidTypeForChapter = new Forest();
        harness.setGraveyard(player1, List.of(shock, invalidTypeForChapter));
        addSagaWithLore(2);

        triggerNextChapter();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = new Permanent(new FoundingTheThirdPath());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
