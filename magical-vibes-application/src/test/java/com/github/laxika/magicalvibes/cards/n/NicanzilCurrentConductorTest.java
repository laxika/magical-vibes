package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IxallisDiviner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NicanzilCurrentConductor.class, IxallisDiviner.class, Forest.class, GrizzlyBears.class})
class NicanzilCurrentConductorTest extends BaseCardTest {

    @Test
    @DisplayName("Exploring a land lets Nicanzil put a land from hand onto the battlefield tapped")
    void landExplorePutsLandFromHandTapped() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        Card exploredCard = new Forest();
        prepareExplore(exploredCard);

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exploredCard && permanent.isTapped());
        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exploring a nonland puts a +1/+1 counter on Nicanzil")
    void nonlandExplorePutsCounterOnNicanzil() {
        Permanent nicanzil = harness.addToBattlefieldAndReturn(player1, new NicanzilCurrentConductor());
        prepareExplore(new GrizzlyBears());

        resolveExploreTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(nicanzil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
    }

    private void prepareExplore(Card exploredCard) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addFirst(exploredCard);
        harness.setHand(player1, List.of(new IxallisDiviner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveExploreTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
