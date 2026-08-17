package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OliviaMobilizedForWarTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card buffs the entering creature and makes it a Vampire")
    void discardCardBuffsEnteringCreature() {
        Permanent bears = triggerOliviaWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Declining the discard leaves the entering creature unchanged")
    void decliningDiscardLeavesEnteringCreatureUnchanged() {
        Permanent bears = triggerOliviaWithHand(new Forest());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(bears.getGrantedSubtypes()).doesNotContain(CardSubtype.VAMPIRE);
    }

    private Permanent triggerOliviaWithHand(Forest discardedCard) {
        harness.addToBattlefield(player1, new OliviaMobilizedForWar());
        harness.setHand(player1, List.of(new GrizzlyBears(), discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return findPermanent(player1, "Grizzly Bears");
    }
}
