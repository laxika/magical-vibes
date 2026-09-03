package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.cards.s.SoldeviDigger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrovikanPlague.class, KjeldoranEscort.class, ShieldSphere.class, SoldeviDigger.class})
class KrovikanPlagueTest extends BaseCardTest {

    private Permanent host;

    private void setupPlagueOnEscort() {
        host = addCreatureReady(player1, new KjeldoranEscort());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new KrovikanPlague());
        aura.setAttachedTo(host.getId());
    }

    @Test
    @DisplayName("Tapping the enchanted creature deals 1 damage and puts a -0/-1 counter on it")
    void dealsDamageAndShrinksHost() {
        setupPlagueOnEscort();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(host.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can deal damage to a creature target")
    void dealsDamageToCreatureTarget() {
        setupPlagueOnEscort();
        Permanent target = addCreatureReady(player2, new KjeldoranEscort());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while the enchanted creature is tapped")
    void cannotActivateWithTappedHost() {
        setupPlagueOnEscort();
        host.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering the battlefield schedules a draw at the next upkeep")
    void schedulesDelayedDraw() {
        Permanent bears = addCreatureReady(player1, new KjeldoranEscort());
        harness.setHand(player1, List.of(new KrovikanPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot enchant a Wall")
    void cannotEnchantWall() {
        Permanent wall = addCreatureReady(player1, new ShieldSphere());
        harness.setHand(player1, List.of(new KrovikanPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a creature an opponent controls")
    void cannotEnchantOpponentCreature() {
        Permanent theirs = addCreatureReady(player2, new KjeldoranEscort());
        harness.setHand(player1, List.of(new KrovikanPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, theirs.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SoldeviDigger());
        harness.setHand(player1, List.of(new KrovikanPlague()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
