package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OliviaMobilizedForWar.class, DevilthornFox.class, Forest.class})
class OliviaMobilizedForWarTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card buffs the entering creature and makes it a Vampire")
    void discardCardBuffsEnteringCreature() {
        Permanent fox = triggerOliviaWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, fox, Keyword.HASTE)).isTrue();
        assertThat(fox.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Declining the discard leaves the entering creature unchanged")
    void decliningDiscardLeavesEnteringCreatureUnchanged() {
        Permanent fox = triggerOliviaWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, fox, Keyword.HASTE)).isFalse();
        assertThat(fox.getGrantedSubtypes()).doesNotContain(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("The counter, haste, and Vampire subtype apply before priority after the discard")
    void discardBenefitsApplyDuringSameResolution() {
        Permanent fox = triggerOliviaWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, fox, Keyword.HASTE)).isTrue();
        assertThat(fox.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste expires but the counter and Vampire subtype remain next turn")
    void onlyHasteExpiresAtEndOfTurn() {
        Permanent fox = triggerOliviaWithHand(new Forest());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, fox, Keyword.HASTE)).isFalse();
        assertThat(fox.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("An empty hand cannot pay for Olivia's benefits")
    void emptyHandDoesNotGrantBenefits() {
        harness.addToBattlefield(player1, new OliviaMobilizedForWar());
        harness.castFromHand(player1, new DevilthornFox(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        Permanent fox = findPermanent(player1, "Devilthorn Fox");
        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, fox, Keyword.HASTE)).isFalse();
        assertThat(fox.getGrantedSubtypes()).doesNotContain(CardSubtype.VAMPIRE);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Olivia does not trigger for her own entrance")
    void doesNotTriggerForHerself() {
        harness.castFromHand(player1, new OliviaMobilizedForWar(), "{1}{B}{R}");
        harness.setHand(player1, List.of(new Forest()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Olivia, Mobilized for War");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Olivia does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player2, new OliviaMobilizedForWar());
        harness.setHand(player2, List.of(new Forest()));
        harness.castFromHand(player1, new DevilthornFox(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devilthorn Fox");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player2, "Forest");
    }

    private Permanent triggerOliviaWithHand(Forest discardedCard) {
        harness.addToBattlefield(player1, new OliviaMobilizedForWar());
        harness.castFromHand(player1, new DevilthornFox(), "{1}{W}");
        harness.setHand(player1, List.of(discardedCard));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return findPermanent(player1, "Devilthorn Fox");
    }
}
