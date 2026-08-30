package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlissaSunslayerTest extends BaseCardTest {

    private static final String DRAW_AND_LOSE = "You draw a card and lose 1 life";
    private static final String DESTROY_ENCHANTMENT = "Destroy target enchantment";
    private static final String REMOVE_COUNTERS = "Remove up to three counters from target permanent";

    @Test
    @DisplayName("Combat damage mode draws a card and loses 1 life")
    void drawAndLoseLifeMode() {
        addReadyGlissa().setAttacking(true);
        harness.setLife(player1, 20);
        setDeck(player1, List.of(new HillGiant()));
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
        addReadyGlissa().setAttacking(true);
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
        addReadyGlissa().setAttacking(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DESTROY_ENCHANTMENT);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter mode removes up to three chosen counters, including mixed kinds")
    void removesChosenMixedCounters() {
        addReadyGlissa().setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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
        addReadyGlissa().setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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

    private Permanent addReadyGlissa() {
        Permanent glissa = new Permanent(new GlissaSunslayer());
        glissa.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(glissa);
        return glissa;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
