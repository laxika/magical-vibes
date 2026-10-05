package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.ScytheLeopard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EmblemControllerLosesLifeOnAnyPlayerDrawEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObNixilisReignited.class, ScytheLeopard.class})
class ObNixilisReignitedTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card and loses 1 life")
    void plusOneDrawsAndLosesLife() {
        addReadyObNixilis(player1, 5);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new ScytheLeopard()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Ob Nixilis Reignited")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 destroys target creature")
    void minusThreeDestroysTargetCreature() {
        Permanent obNixilis = addReadyObNixilis(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ScytheLeopard());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scythe Leopard");
        harness.assertInGraveyard(player2, "Scythe Leopard");
        assertThat(obNixilis.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a noncreature permanent")
    void minusThreeCannotTargetNoncreature() {
        addReadyObNixilis(player1, 5);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new ObNixilisReignited());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 gives an opponent an emblem that triggers for either player's draw")
    void minusEightEmblemTriggersForEitherPlayerDraw() {
        addReadyObNixilis(player1, 8);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player2.getId());
        assertThat(emblem.staticEffects()).singleElement()
                .isEqualTo(new EmblemControllerLosesLifeOnAnyPlayerDrawEffect(2));

        harness.setLibrary(player1, List.of(new ScytheLeopard()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.setLibrary(player2, List.of(new ScytheLeopard()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("-8 cannot target its controller")
    void minusEightCannotTargetItsController() {
        addReadyObNixilis(player1, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 can destroy a creature controlled by its controller")
    void minusThreeCanDestroyOwnCreature() {
        addReadyObNixilis(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScytheLeopard());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scythe Leopard");
        harness.assertInGraveyard(player1, "Scythe Leopard");
    }

    @Test
    @DisplayName("The emblem triggers separately for every card drawn and survives its source")
    void emblemTriggersForEveryCardAfterSourceLeaves() {
        addReadyObNixilis(player1, 8);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ob Nixilis Reignited");
        harness.assertInGraveyard(player1, "Ob Nixilis Reignited");

        harness.setLibrary(player1, List.of(new ScytheLeopard(), new ScytheLeopard()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("-8 cannot be activated with fewer than eight loyalty counters")
    void ultimateRequiresEnoughLoyalty() {
        Permanent obNixilis = addReadyObNixilis(player1, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(obNixilis.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.emblems).isEmpty();
    }

    private Permanent addReadyObNixilis(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ObNixilisReignited());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
