package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanWayfarer.class, NantukoMonastery.class, IronshellBeetle.class})
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
    void sacrificeAbilityCanBeActivatedWhileSummoningSick() {
        Permanent wayfarer = new Permanent(new KrosanWayfarer());
        wayfarer.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(wayfarer);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Krosan Wayfarer");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void acceptingMayPutsLandFromHandOntoBattlefieldUntapped() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery land = new NantukoMonastery();
        IronshellBeetle creature = new IronshellBeetle();
        harness.setHand(player1, List.of(land, creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        Permanent battlefieldLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == land)
                .findFirst()
                .orElseThrow();
        assertThat(battlefieldLand.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void decliningMayLeavesLandInHand() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery land = new NantukoMonastery();
        harness.setHand(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        harness.assertNotOnBattlefield(player1, "Nantuko Monastery");
    }
}
