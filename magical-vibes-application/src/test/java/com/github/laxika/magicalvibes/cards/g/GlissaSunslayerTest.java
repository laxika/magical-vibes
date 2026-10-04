package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FleshlessGladiator;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlissaSunslayer.class, FleshlessGladiator.class, PhyrexianArena.class})
class GlissaSunslayerTest extends BaseCardTest {

    private static final String DRAW_AND_LOSE = "You draw a card and lose 1 life";
    private static final String DESTROY_ENCHANTMENT = "Destroy target enchantment";
    private static final String REMOVE_COUNTERS = "Remove up to three counters from target permanent";

    @Test
    @DisplayName("Combat damage mode draws a card and loses 1 life")
    void drawAndLoseLifeMode() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new FleshlessGladiator()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DRAW_AND_LOSE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Combat damage mode destroys a targeted enchantment")
    void destroysEnchantment() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DESTROY_ENCHANTMENT);
        harness.handlePermanentChosen(player1, arena.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
    }

    @Test
    @DisplayName("Destroy-enchantment mode rejects a non-enchantment target")
    void destroyEnchantmentRejectsCreatureTarget() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FleshlessGladiator());
        harness.addToBattlefield(player2, new PhyrexianArena());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DESTROY_ENCHANTMENT);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter mode removes up to three chosen counters, including mixed kinds")
    void removesChosenMixedCounters() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FleshlessGladiator());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 2);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTERS);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("+1/+1 counters", "charge counters", "Done");
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.handleListChoice(player1, "charge counters");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counter mode may remove fewer than three counters")
    void removesFewerCounters() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FleshlessGladiator());
        target.setCounterCount(CounterType.CHARGE, 2);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTERS);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "Done");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode and target are chosen before the combat damage trigger resolves")
    void choosesModeAndTargetBeforeResolution() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.handleListChoice(player1, DESTROY_ENCHANTMENT);
        harness.handlePermanentChosen(player1, arena.getId());

        harness.assertOnBattlefield(player2, "Phyrexian Arena");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }

    @Test
    @DisplayName("Counter mode may remove zero counters from your own permanent")
    void mayRemoveZeroCounters() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FleshlessGladiator());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTERS);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Done");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counter mode can target an enchantment without counters")
    void mayTargetPermanentWithoutCounters() {
        addCreatureReady(player1, new GlissaSunslayer()).setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTERS);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Phyrexian Arena");
        assertThat(target.getTotalCounterCount()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
