package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthbenderAscension.class, Forest.class, GrizzlyBears.class})
class EarthbenderAscensionTest extends BaseCardTest {

    @Test
    void entersByEarthbendingExistingLandThenSearchingTappedBasicLand() {
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new EarthbenderAscension()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice earthbendChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(earthbendChoice.validIds()).containsExactly(existingLand.getId());
        harness.handlePermanentChosen(player1, existingLand.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.isCreature(gd, existingLand)).isTrue();
        assertThat(existingLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND)
                        && permanent.isTapped()
                        && permanent != existingLand);
    }

    @Test
    void landfallAtFourQuestCountersTargetsOwnCreatureForCounterAndTrample() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        for (int i = 0; i < 4; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.passBothPriorities();
        }

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }
}
