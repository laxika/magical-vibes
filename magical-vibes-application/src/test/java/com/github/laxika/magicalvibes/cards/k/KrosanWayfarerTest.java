package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanWayfarer.class, NantukoMonastery.class})
class KrosanWayfarerTest extends BaseCardTest {

    @Test
    void sacrificingWayfarerIsPaidBeforeAbilityResolves() {
        addCreatureReady(player1, new KrosanWayfarer());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Krosan Wayfarer");
        harness.assertNotOnBattlefield(player1, "Krosan Wayfarer");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void acceptingMayPutsLandFromHandOntoBattlefieldUntapped() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery landCard = new NantukoMonastery();
        KrosanWayfarer nonlandCard = new KrosanWayfarer();
        harness.setHand(player1, List.of(landCard, nonlandCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == landCard)
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonlandCard);
    }

    @Test
    void decliningMayLeavesLandInHand() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery landCard = new NantukoMonastery();
        harness.setHand(player1, List.of(landCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(landCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == landCard);
    }

    @Test
    void acceptingMayWithNoLandLeavesHandUnchanged() {
        addCreatureReady(player1, new KrosanWayfarer());
        KrosanWayfarer nonlandCard = new KrosanWayfarer();
        harness.setHand(player1, List.of(nonlandCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonlandCard);
        harness.assertInGraveyard(player1, "Krosan Wayfarer");
    }
}
