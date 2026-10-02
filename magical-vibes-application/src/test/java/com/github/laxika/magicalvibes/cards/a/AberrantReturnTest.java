package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarruksCompanion;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AberrantReturn.class, GrizzlyBears.class, Forest.class,
        GarruksCompanion.class, GarruksPackleader.class})
class AberrantReturnTest extends BaseCardTest {

    @Test
    void returnsUpToThreeCreatureCardsFromAnyGraveyardUnderYourControlWithCounters() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId(), opponentCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(ownCreature.getId(), opponentCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(ownCreature.getId(), opponentCreature.getId());
        assertThat(findReturned(ownCreature).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(findReturned(opponentCreature).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void rejectsCastingWhenNoCreatureCardsAreAvailable() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseOnlyOneOfSeveralAvailableCreatures() {
        Card chosen = new GrizzlyBears();
        Card unchosen = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen, unchosen));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(findReturned(chosen).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void returnsThreeCreaturesAndLeavesTheFourthInItsGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(third, fourth));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        for (Card returned : List.of(first, second, third)) {
            assertThat(findReturned(returned).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        }
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(fourth);
    }

    @Test
    void rejectsZeroFourDuplicateAndNoncreatureTargets() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(first, second, third, fourth, land));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findReturned(first).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void stillReturnsTheLegalTargetWhenAnotherTargetLeavesTheGraveyard() {
        Card remaining = new GrizzlyBears();
        Card removed = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(remaining));
        harness.setGraveyard(player2, List.of(removed));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findReturned(remaining).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(removed);
    }

    @Test
    void doesNotReturnAnythingWhenAllTargetsLeaveTheGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Aberrant Return");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({AberrantReturn.class, GarruksPackleader.class, GarruksCompanion.class})
    void entryTriggersSeeTheCreatureWithItsMinusOneCounterAlreadyPresent() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        Card creature = new GarruksCompanion();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findReturned(creature).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({AberrantReturn.class, GarruksPackleader.class})
    void creaturesReturnedTogetherSeeEachOthersEntryForTriggeredAbilities() {
        Card first = new GarruksPackleader();
        Card second = new GarruksPackleader();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player1, List.of(new AberrantReturn()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findReturned(first).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(findReturned(second).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent findReturned(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
