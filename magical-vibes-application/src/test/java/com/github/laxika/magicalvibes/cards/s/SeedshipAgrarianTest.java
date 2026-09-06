package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedshipAgrarian.class, Forest.class})
class SeedshipAgrarianTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Lander when Seedship Agrarian becomes tapped")
    void createsLanderWhenBecomingTapped() {
        Permanent agrarian = harness.addToBattlefieldAndReturn(player1, new SeedshipAgrarian());

        tap(agrarian);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Seedship Agrarian")
    void landfallPutsCounterOnSelf() {
        Permanent agrarian = harness.addToBattlefieldAndReturn(player1, new SeedshipAgrarian());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(agrarian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Lander searches for a basic Forest and puts it onto the battlefield tapped")
    void landerSearchesForTappedBasicLand() {
        Permanent agrarian = harness.addToBattlefieldAndReturn(player1, new SeedshipAgrarian());
        tap(agrarian);
        harness.passBothPriorities();

        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId())
                        && permanent.isTapped()
                        && permanent.getCard().hasType(CardType.LAND));
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
