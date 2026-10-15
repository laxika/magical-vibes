package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderIslanders.class})
class SpiderIslandersTest extends BaseCardTest {

    @Test
    @DisplayName("Mayhem casts Spider-Islanders from the graveyard after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        SpiderIslanders card = new SpiderIslanders();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(permanent.getOriginalCard()).isSameAs(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Mayhem cannot cast Spider-Islanders from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new SpiderIslanders()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayhemRequiresThisSpecificCardToHaveBeenDiscarded() {
        SpiderIslanders card = new SpiderIslanders();
        SpiderIslanders otherCard = new SpiderIslanders();
        harness.setGraveyard(player1, List.of(card, otherCard));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(otherCard.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card, otherCard);
    }

    @Test
    void mayhemCannotBeCastOutsideMainPhase() {
        SpiderIslanders card = prepareMayhemCard();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void mayhemCannotBeCastDuringOpponentsTurn() {
        SpiderIslanders card = prepareMayhemCard();
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void mayhemCannotBeCastWithAnotherSpellOnTheStack() {
        SpiderIslanders card = prepareMayhemCard();
        harness.setHand(player1, List.of(new SpiderIslanders()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void mayhemRequiresRedMana() {
        SpiderIslanders card = prepareMayhemCard();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void mayhemRequiresTheFullAlternateCost() {
        SpiderIslanders card = prepareMayhemCard();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void mayhemCostDoesNotApplyWhenCastingFromHand() {
        harness.setHand(player1, List.of(new SpiderIslanders()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private SpiderIslanders prepareMayhemCard() {
        SpiderIslanders card = new SpiderIslanders();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        return card;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
