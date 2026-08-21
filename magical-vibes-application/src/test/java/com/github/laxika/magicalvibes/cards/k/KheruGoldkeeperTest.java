package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KheruGoldkeeper.class, GrizzlyBears.class, Mountain.class, Reminisce.class})
class KheruGoldkeeperTest extends BaseCardTest {

    @Test
    void createsOneTreasureWhenMultipleCardsLeaveGraveyardDuringYourTurn() {
        addReadyGoldkeeper(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotCreateTreasureDuringOpponentTurn() {
        addReadyGoldkeeper(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.setHand(player2, List.of(new Reminisce()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void renewPutsTwoPlusOneCountersAndFlyingCounterOnTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Kheru Goldkeeper");
    }

    @Test
    void renewRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void renewIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyRenew();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyGoldkeeper(Player player) {
        harness.addToBattlefield(player, new KheruGoldkeeper());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void readyRenew() {
        harness.setGraveyard(player1, List.of(new KheruGoldkeeper()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
