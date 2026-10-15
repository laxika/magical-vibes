package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuramancersGuise;
import com.github.laxika.magicalvibes.cards.d.DustOfMoments;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Calciderm.class, AuramancersGuise.class, CauterySliver.class, DustOfMoments.class})
class CalcidermTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four time counters")
    void entersWithTimeCounters() {
        harness.castFromHand(player1, new Calciderm(), "{2}{W}{W}");
        harness.passBothPriorities();

        Permanent calciderm = findPermanent(player1, "Calciderm");

        assertThat(calciderm.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(calciderm.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(calciderm);
    }

    @Test
    @DisplayName("Does not remove a time counter during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(calciderm.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Calciderm");
        harness.assertInGraveyard(player1, "Calciderm");
    }

    @Test
    @DisplayName("Does not sacrifice again when it has no time counters")
    void noTimeCountersDoesNotTriggerSacrifice() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(calciderm);
    }

    @Test
    @DisplayName("Vanishing does not trigger at upkeep without time counters")
    void noTimeCountersDoesNotCreateUpkeepTrigger() {
        addCreatureReady(player1, new Calciderm());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Calciderm.class, DustOfMoments.class})
    @DisplayName("Removing the last time counters with another spell causes sacrifice")
    void externalRemovalOfLastTimeCountersCausesSacrifice() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new DustOfMoments()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        harness.castModalInstant(player2, 0, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Calciderm");
        harness.assertInGraveyard(player1, "Calciderm");
    }

    @Test
    @DisplayName("Cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());

        harness.setHand(player1, List.of(new AuramancersGuise()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, calciderm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Cannot be targeted by activated abilities because it has shroud")
    void cannotBeTargetedByAbilities() {
        addCreatureReady(player1, new CauterySliver());
        Permanent calciderm = addCreatureReady(player2, new Calciderm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, calciderm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
