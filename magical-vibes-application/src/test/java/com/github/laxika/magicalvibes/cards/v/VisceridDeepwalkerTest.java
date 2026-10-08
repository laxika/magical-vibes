package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({VisceridDeepwalker.class, TeferiMageOfZhalfir.class, PithingNeedle.class})
class VisceridDeepwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Viscerid Deepwalker with four time counters")
    void suspendExilesWithFourTimeCounters() {
        VisceridDeepwalker card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend counters are removed only during the owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        VisceridDeepwalker card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("Without flash, suspend is unavailable during upkeep")
    void suspendRequiresSorcerySpeed() {
        VisceridDeepwalker card = new VisceridDeepwalker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        VisceridDeepwalker card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Viscerid Deepwalker");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Viscerid Deepwalker in exile")
    void decliningCastLeavesCardInExile() {
        VisceridDeepwalker card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(card);
    }

    @Test
    @DisplayName("The blue ability gives Viscerid Deepwalker +1/+0 until end of turn")
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent permanent = addCreatureReady(player1, new VisceridDeepwalker());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(permanent.getPowerModifier()).isEqualTo(1);
        assertThat(permanent.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(permanent.getPowerModifier()).isZero();
        assertThat(permanent.getToughnessModifier()).isZero();
    }

    private VisceridDeepwalker suspendCard() {
        VisceridDeepwalker card = new VisceridDeepwalker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    @Test
    @DisplayName("Removing a suspend counter waits for the upkeep trigger to resolve")
    void upkeepCounterRemovalUsesTheStack() {
        VisceridDeepwalker card = suspendCard();

        advanceToUpkeep(player1);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flash allows suspending during an opponent's upkeep")
    void flashAllowsSuspendOutsideSorceryTiming() {
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        advanceToUpkeep(player2);
        VisceridDeepwalker card = new VisceridDeepwalker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend requires its blue mana payment")
    void suspendWithoutManaLeavesCardInHand() {
        VisceridDeepwalker card = new VisceridDeepwalker();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    @DisplayName("Repeated pump activations stack and affect only their source")
    void multiplePumpActivationsAffectOnlySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VisceridDeepwalker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new VisceridDeepwalker());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(source.getPowerModifier()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Casting normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.castFromHand(player1, new VisceridDeepwalker(), "{4}{U}");
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Viscerid Deepwalker");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Pithing Needle cannot prohibit the suspend special action")
    void activatedAbilityLockDoesNotPreventSuspend() {
        harness.castFromHand(player1, new PithingNeedle(), "{1}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Viscerid Deepwalker");

        VisceridDeepwalker card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

}
