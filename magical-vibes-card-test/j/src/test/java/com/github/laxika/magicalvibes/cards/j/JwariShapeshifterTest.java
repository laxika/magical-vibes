package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JwariShapeshifter.class, HadaFreeblade.class, GrizzlyBears.class})
class JwariShapeshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Can enter as a copy of an Ally creature")
    void copiesAllyCreature() {
        harness.addToBattlefield(player2, new HadaFreeblade());
        harness.castFromHand(player1, new JwariShapeshifter(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID allyId = harness.getPermanentId(player2, "Hada Freeblade");
        harness.handlePermanentChosen(player1, allyId);

        Permanent shapeshifter = findPermanent(player1, "Hada Freeblade");

        assertThat(shapeshifter).isNotNull();
        assertThat(shapeshifter.getCard().getName()).isEqualTo("Hada Freeblade");
        assertThat(shapeshifter.getCard().getPower()).isEqualTo(0);
        assertThat(shapeshifter.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not copy a non-Ally creature")
    void doesNotCopyNonAllyCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new JwariShapeshifter(), "{1}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Jwari Shapeshifter");
        harness.assertInGraveyard(player1, "Jwari Shapeshifter");
    }

    @Test
    @DisplayName("May decline copying even when an Ally is available")
    void mayDeclineCopy() {
        harness.addToBattlefield(player2, new HadaFreeblade());
        harness.castFromHand(player1, new JwariShapeshifter(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Jwari Shapeshifter");
        harness.assertInGraveyard(player1, "Jwari Shapeshifter");
        harness.assertOnBattlefield(player2, "Hada Freeblade");
    }

    @Test
    @DisplayName("Dies without copying when the battlefield is empty")
    void noAllyAvailable() {
        harness.castFromHand(player1, new JwariShapeshifter(), "{1}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Jwari Shapeshifter");
        harness.assertInGraveyard(player1, "Jwari Shapeshifter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copied Ally ability triggers on entry without copying existing counters")
    void copiedAllyTriggersWithoutCopyingCounters() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new HadaFreeblade());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        original.tap();
        harness.castFromHand(player1, new JwariShapeshifter(), "{1}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        Permanent copy = findPermanent(player1, "Hada Freeblade");
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
