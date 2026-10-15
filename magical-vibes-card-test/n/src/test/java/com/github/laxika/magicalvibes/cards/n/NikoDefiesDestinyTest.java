package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.d.DoomskarOracle;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NikoDefiesDestiny.class, AuguryRaven.class, DoomskarOracle.class, FearlessPup.class})
class NikoDefiesDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gains 2 life for each foretold card owned in exile")
    void chapterIGainsLifeForForetoldCards() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.setLife(player1, 20);
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Chapter I counts multiple foretold cards but not ordinary exiled cards")
    void chapterICountsOnlyForetoldCards() {
        harness.setExile(player1, List.of(new AuguryRaven(), new FearlessPup()));
        harness.setHand(player1, List.of(new AuguryRaven(), new DoomskarOracle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.foretell(player1, 0);
        harness.foretell(player1, 0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chapter I ignores foretold cards owned by the opponent")
    void chapterIIgnoresOpponentsForetoldCards() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AuguryRaven()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.foretell(player2, 0);
        harness.setLife(player1, 20);
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Chapter II mana pays the entire foretell special action cost")
    void chapterIIManaPaysForetell() {
        DoomskarOracle oracle = new DoomskarOracle();
        harness.setHand(player1, List.of(oracle));
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.foretell(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(oracle);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter II mana pays for a spell with foretell cast from hand")
    void chapterIIManaPaysNormalForetellSpellCost() {
        harness.setHand(player1, List.of(new DoomskarOracle()));
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doomskar Oracle");
    }

    @Test
    @DisplayName("Chapter II mana pays for a card foretold on an earlier turn")
    void chapterIIManaPaysPreviouslyForetoldSpell() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.castFromExile(player1, raven.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Augury Raven");
    }

    @Test
    @DisplayName("Chapter II mana cannot pay for a spell without foretell")
    void chapterIIManaCannotPayForNonForetellSpell() {
        harness.setHand(player1, List.of(new NikoDefiesDestiny()));
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III returns only a card with foretell")
    void chapterIIIReturnsCardWithForetell() {
        FearlessPup nonForetellCard = new FearlessPup();
        AuguryRaven foretellCard = new AuguryRaven();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(nonForetellCard, foretellCard));
        addSaga(2);

        triggerChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(foretellCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(foretellCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(foretellCard);
    }

    @Test
    @DisplayName("Chapter III excludes the opponent's graveyard and sacrifices after resolving")
    void chapterIIIOnlyTargetsControllersGraveyard() {
        AuguryRaven ownCard = new AuguryRaven();
        DoomskarOracle opposingCard = new DoomskarOracle();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        addSaga(2);

        triggerChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.assertOnBattlefield(player1, "Niko Defies Destiny");
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        harness.assertNotOnBattlefield(player1, "Niko Defies Destiny");
        harness.assertInGraveyard(player1, "Niko Defies Destiny");
    }

    @Test
    @DisplayName("Chapter III cannot return a target that leaves the graveyard before resolution")
    void chapterIIITargetLeavesGraveyard() {
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(raven));
        addSaga(2);

        triggerChapter();
        harness.handleMultipleCardsChosen(player1, List.of(raven.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(raven));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(raven);
        harness.assertNotOnBattlefield(player1, "Niko Defies Destiny");
    }

    private void addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NikoDefiesDestiny());
        saga.setCounterCount(CounterType.LORE, loreCounters);
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
