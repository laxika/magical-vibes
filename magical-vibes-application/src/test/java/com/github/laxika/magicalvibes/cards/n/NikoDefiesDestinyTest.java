package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.d.DoomskarOracle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NikoDefiesDestiny.class, AuguryRaven.class, DoomskarOracle.class, GrizzlyBears.class})
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
    @DisplayName("Chapter II mana pays both foretell and a later foretell spell")
    void chapterIIManaPaysForetellAndForetellSpell() {
        DoomskarOracle oracle = new DoomskarOracle();
        harness.setHand(player1, List.of(oracle));
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        gd.turnNumber++;
        harness.castFromExile(player1, oracle.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doomskar Oracle");
    }

    @Test
    @DisplayName("Chapter III returns only a card with foretell")
    void chapterIIIReturnsCardWithForetell() {
        GrizzlyBears nonForetellCard = new GrizzlyBears();
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

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NikoDefiesDestiny());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
