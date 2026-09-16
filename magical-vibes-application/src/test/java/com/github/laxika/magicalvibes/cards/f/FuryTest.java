package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fury.class, GrizzlyBears.class, GarrukWildspeaker.class})
class FuryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB divides 4 damage among target creatures and planeswalkers")
    void etbDealsDividedDamageAmongCreaturesAndPlaneswalkers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 1, planeswalker.getId(), 3);

        harness.setHand(player1, List.of(new Fury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Fury");
    }

    @Test
    @DisplayName("Evoke exiles a red card, resolves the ETB, and sacrifices Fury")
    void evokeExilesRedCardAndSacrificesSelf() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card redCard = new Fury();
        gd.pendingETBDamageAssignments = Map.of(target.getId(), 4);
        harness.setHand(player1, List.of(new Fury(), redCard));

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(target.getId()), List.of(), false, null, null, List.of(), null, List.of(), false, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Fury");
        harness.assertInGraveyard(player1, "Fury");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName()).containsExactly("Fury");
    }

    @Test
    @DisplayName("ETB cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Fury()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
