package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakuraTribeScout.class, MikokoroCenterOfTheSea.class})
class SakuraTribeScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a land from hand onto the battlefield untapped")
    void putsLandOntoBattlefield() {
        Permanent scout = addCreatureReady(player1, new SakuraTribeScout());
        SakuraTribeScout nonland = new SakuraTribeScout();
        MikokoroCenterOfTheSea landCard = new MikokoroCenterOfTheSea();
        harness.setHand(player1, List.of(nonland, landCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(scout.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        Permanent land = findPermanent(player1, "Mikokoro, Center of the Sea");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("May decline putting a land from hand onto the battlefield")
    void mayDecline() {
        addCreatureReady(player1, new SakuraTribeScout());
        MikokoroCenterOfTheSea landCard = new MikokoroCenterOfTheSea();
        harness.setHand(player1, List.of(landCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not put a nonland card from hand onto the battlefield")
    void doesNotPutNonlandCard() {
        addCreatureReady(player1, new SakuraTribeScout());
        SakuraTribeScout nonland = new SakuraTribeScout();
        harness.setHand(player1, List.of(nonland));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
    }
}
