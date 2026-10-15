package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HarvestFear;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReaperOfNight.class, HarvestFear.class, SporecapSpider.class})
class ReaperOfNightTest extends BaseCardTest {

    @Test
    void attacksAndGainsFlyingWhenDefendingPlayerHasTwoCardsInHand() {
        harness.setHand(player2, List.of(new SporecapSpider(), new SporecapSpider()));
        Permanent reaper = addCreatureReady(player1, new ReaperOfNight());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotGainFlyingWhenDefendingPlayerHasMoreThanTwoCardsInHand() {
        harness.setHand(player2, List.of(
                new SporecapSpider(), new SporecapSpider(), new SporecapSpider()));
        Permanent reaper = addCreatureReady(player1, new ReaperOfNight());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void adventureMakesTargetOpponentDiscardTwoAndExilesTheCard() {
        ReaperOfNight card = new ReaperOfNight();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(
                new SporecapSpider(), new SporecapSpider(), new SporecapSpider()));
        addAdventureMana();

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetItsController() {
        ReaperOfNight card = new ReaperOfNight();
        harness.setHand(player1, List.of(card));
        addAdventureMana();

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        ReaperOfNight card = new ReaperOfNight();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        addAdventureMana();

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reaper of Night");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void gainsFlyingWhenDefendingPlayerHasNoCardsRegardlessOfControllersHand() {
        harness.setHand(player1, List.of(new SporecapSpider(), new SporecapSpider(), new SporecapSpider()));
        harness.setHand(player2, List.of());
        Permanent reaper = addCreatureReady(player1, new ReaperOfNight());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotGainFlyingIfDefendingHandGrowsBeforeTriggerResolves() {
        harness.setHand(player2, List.of(new SporecapSpider(), new SporecapSpider()));
        Permanent reaper = addCreatureReady(player1, new ReaperOfNight());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.setHand(player2, List.of(new SporecapSpider(), new SporecapSpider(), new SporecapSpider()));
            harness.passBothPriorities();
        });

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotTriggerIfDefendingHandOnlyShrinksAfterAttackDeclaration() {
        harness.setHand(player2, List.of(new SporecapSpider(), new SporecapSpider(), new SporecapSpider()));
        Permanent reaper = addCreatureReady(player1, new ReaperOfNight());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).isEmpty();
            harness.setHand(player2, List.of());
            harness.passBothPriorities();
        });

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void keepsGrantedFlyingIfDefendingHandGrowsAfterResolution() {
        harness.setHand(player2, List.of(new SporecapSpider()));
        Permanent reaper = addCreatureReady(player1, new ReaperOfNight());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            assertThat(reaper.hasKeyword(Keyword.FLYING)).isTrue();
            harness.setHand(player2, List.of(new SporecapSpider(), new SporecapSpider(), new SporecapSpider()));
        });

        assertThat(reaper.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void adventureDiscardsTheOnlyCardInOpponentsHandAndStillExiles() {
        ReaperOfNight card = new ReaperOfNight();
        SporecapSpider discarded = new SporecapSpider();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(discarded));
        addAdventureMana();

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void creatureCanBeCastDirectlyWithoutDiscardingOpponentsCards() {
        ReaperOfNight card = new ReaperOfNight();
        SporecapSpider opponentCard = new SporecapSpider();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reaper of Night");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    private void addAdventureMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
