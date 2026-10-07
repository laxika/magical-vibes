package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TouchOfDeath.class, JaceBeleren.class, ScatheZombies.class})
class TouchOfDeathTest extends BaseCardTest {

    private void cast() {
        castWithTarget(player2.getId());
    }

    @Test
    @DisplayName("Deals 1 damage to the target player and controller gains 1 life")
    void dealsDamageAndGainsLife() {
        int targetBefore = gd.getLife(player2.getId());
        int controllerBefore = gd.getLife(player1.getId());

        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(targetBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerBefore + 1);
    }

    @Test
    @DisplayName("Can target its controller and still gain 1 life")
    void canTargetController() {
        int controllerBefore = gd.getLife(player1.getId());
        int targetBefore = gd.getLife(player2.getId());

        castWithTarget(player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetBefore);
    }

    @Test
    @DisplayName("Controller gains life when the damage defeats the target")
    void controllerGainsLifeWhenDamageDefeatsTarget() {
        int controllerBefore = gd.getLife(player1.getId());
        harness.setLife(player2, 1);

        cast();

        assertThat(gd.getLife(player2.getId())).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerBefore + 1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Deals 1 damage to a targeted planeswalker")
    void dealsDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        int targetLifeBefore = gd.getLife(player2.getId());
        castWithTarget(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore);
    }

    @Test
    @DisplayName("Schedules a draw at the next upkeep, resolving there")
    void schedulesAndResolvesDraw() {
        cast();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("An illegal sole target prevents life gain and the delayed draw")
    void removedPlaneswalkerTargetStopsAllEffects() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new TouchOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, planeswalker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.setGraveyard(player2, List.of(planeswalker.getCard()));

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TouchOfDeath);
    }

    @Test
    @DisplayName("Targeting yourself at one life finishes resolution before checking for defeat")
    void selfTargetAtOneLifeSurvives() {
        harness.setLife(player1, 1);

        castWithTarget(player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Creatures are not legal targets")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new TouchOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithTarget(UUID targetId) {
        harness.setHand(player1, List.of(new TouchOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B}
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
