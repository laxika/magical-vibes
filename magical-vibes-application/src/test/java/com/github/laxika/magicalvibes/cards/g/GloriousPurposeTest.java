package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HypnoticGrifter;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({GloriousPurpose.class, HypnoticGrifter.class, Forest.class, Mountain.class})
class GloriousPurposeTest extends BaseCardTest {

    @Test
    void connivingCreatureGetsCounterAndPurposeGetsPlanCounter() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        Permanent grifter = addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new HypnoticGrifter()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(purpose.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    void sixthPlanCounterSacrificesPurposeAndReturnsUncastCardsToHand() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        purpose.setCounterCount(CounterType.PLAN, 5);
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Mountain()));
        Card drawnForConnive = new Mountain();
        Card freeCastCreature = new HypnoticGrifter();
        Card firstRest = new Forest();
        Card secondRest = new Forest();
        Card thirdRest = new Forest();
        harness.setLibrary(player1, List.of(drawnForConnive, freeCastCreature, firstRest, secondRest, thirdRest));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() == purpose.getCard());
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactly(freeCastCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(freeCastCreature.getId()));

        assertThat(gd.findExiledCard(freeCastCreature.getId())).isNull();
        assertThat(gd.findExiledCard(firstRest.getId())).isNull();
        assertThat(gd.findExiledCard(secondRest.getId())).isNull();
        assertThat(gd.findExiledCard(thirdRest.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstRest, secondRest, thirdRest);
    }
}
