package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RiteOfBelzenlok;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheManyDeedsOfBelzenlok.class, RiteOfBelzenlok.class})
class TheManyDeedsOfBelzenlokTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I can exile a Saga from any graveyard and copy its chapter I ability")
    void chapterICopiesSagaChapterFromOpponentGraveyard() {
        RiteOfBelzenlok rite = new RiteOfBelzenlok();
        harness.setGraveyard(player2, List.of(rite));
        harness.setHand(player1, List.of(new TheManyDeedsOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(rite.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("chapter I"));

        harness.passBothPriorities();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(rite);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Cleric"))
                .hasSize(2).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Chapter I does nothing when no Saga is available in a graveyard")
    void chapterISkipsWithoutSagaTarget() {
        harness.setHand(player1, List.of(new TheManyDeedsOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .extracting("name").containsExactly("The Many Deeds of Belzenlok");
    }

    @Test
    void chapterIICopiesFromControllersGraveyard() {
        RiteOfBelzenlok rite = new RiteOfBelzenlok();
        harness.setGraveyard(player1, List.of(rite));
        advanceToChapter(2);

        harness.handleMultipleCardsChosen(player1, List.of(rite.getId()));
        harness.passBothPriorities();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(rite);
        harness.assertNotInGraveyard(player1, "Rite of Belzenlok");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cleric")).isEqualTo(2);
        assertThat(countPermanents(player2, "Cleric")).isZero();
        harness.assertOnBattlefield(player1, "The Many Deeds of Belzenlok");
    }

    @Test
    void chapterIIICopiesDemonAbilityAndSacrificesSaga() {
        RiteOfBelzenlok rite = new RiteOfBelzenlok();
        harness.setGraveyard(player2, List.of(rite));
        advanceToChapter(3);

        harness.handleMultipleCardsChosen(player1, List.of(rite.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "The Many Deeds of Belzenlok");
        harness.assertNotOnBattlefield(player1, "The Many Deeds of Belzenlok");
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(rite);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
        assertThat(countPermanents(player2, "Demon")).isZero();
        assertThat(countPermanents(player1, "Cleric")).isZero();
    }

    @Test
    void canDeclineExilingAnAvailableSaga() {
        RiteOfBelzenlok rite = new RiteOfBelzenlok();
        harness.setGraveyard(player1, List.of(rite));
        harness.setHand(player1, List.of(new TheManyDeedsOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rite of Belzenlok");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cleric")).isZero();
    }

    @Test
    void removedTargetIsNotExiledOrCopied() {
        RiteOfBelzenlok rite = new RiteOfBelzenlok();
        harness.setGraveyard(player2, List.of(rite));
        harness.setHand(player1, List.of(new TheManyDeedsOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(rite.getId()));

        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(rite));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Rite of Belzenlok");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cleric")).isZero();
    }

    private void advanceToChapter(int chapter) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheManyDeedsOfBelzenlok());
        saga.setCounterCount(CounterType.LORE, chapter - 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
