package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScoutForSurvivors.class, EliteVanguard.class, LanternKami.class, SavannahLions.class, HillGiant.class, MentorOfTheMeek.class})
class ScoutForSurvivorsTest extends BaseCardTest {

    @Test
    void returnsUpToThreeCreaturesWithinManaValueLimitWithCounters() {
        Card first = new EliteVanguard();
        Card second = new LanternKami();
        Card third = new SavannahLions();
        Card spell = new ScoutForSurvivors();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.castFromHand(player1, spell, "{2}{W}");

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.maxTotalManaValue()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        for (Card card : List.of(first, second, third)) {
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId())
                            && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);
        }
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(spell.getId());
    }

    @Test
    void rejectsTargetsWhoseTotalManaValueExceedsThree() {
        Card cheap = new EliteVanguard();
        Card expensive = new HillGiant();
        harness.setGraveyard(player1, List.of(cheap, expensive));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(cheap.getId(), expensive.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }

    @Test
    void canChooseNoTargetsEvenWhenCreaturesAreAvailable() {
        Card creature = new SavannahLions();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsOnlyTheSelectedCreatureUntappedWithOneCounter() {
        Card selected = new SavannahLions();
        Card unselected = new SavannahLions();
        harness.setGraveyard(player1, List.of(selected, unselected));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");

        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(selected.getId());
            assertThat(permanent.isTapped()).isFalse();
            assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unselected).doesNotContain(selected);
    }

    @Test
    void excludesNoncreaturesAndOpponentsGraveyard() {
        Card creature = new SavannahLions();
        Card noncreature = new ScoutForSurvivors();
        Card opponentCreature = new SavannahLions();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherLeavesGraveyard() {
        Card removed = new SavannahLions();
        Card remaining = new SavannahLions();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(remaining.getId());
            assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
        assertThat(gd.findExiledCard(removed.getId())).isNotNull();
    }

    @Test
    void enteringPowerIsCheckedBeforeScoutAddsItsCounter() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        Card creature = new SavannahLions();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(creature.getId());
                    assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
                });
        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getCard()).isInstanceOf(MentorOfTheMeek.class));
    }

    @Test
    void rejectsCombinedManaValueEvenWhenEachTargetIsIndividuallyEligible() {
        Card threeMana = new MentorOfTheMeek();
        Card oneMana = new SavannahLions();
        harness.setGraveyard(player1, List.of(threeMana, oneMana));
        harness.castFromHand(player1, new ScoutForSurvivors(), "{2}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactlyInAnyOrder(threeMana.getId(), oneMana.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(threeMana.getId(), oneMana.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(threeMana.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(threeMana.getId());
            assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oneMana);
    }

    @Test
    void canCastWithAnEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        Card spell = new ScoutForSurvivors();

        harness.castFromHand(player1, spell, "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
}
