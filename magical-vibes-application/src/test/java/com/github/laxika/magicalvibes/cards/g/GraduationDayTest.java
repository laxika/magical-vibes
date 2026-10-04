package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraduationDay.class, HillGiant.class, Shock.class, Blaze.class})
class GraduationDayTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature puts a +1/+1 counter on a chosen creature you control")
    void reparteePlacesCounter() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);

        // Repartee triggers and prompts for a creature-you-control target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, giantId);

        resolveAllTriggers();

        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    void creatureTargetingSorceryTriggers() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, 2, giantId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, giantId);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void canRewardOwnCreatureWhenSpellTargetsOpposingCreature() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ownId = harness.getPermanentId(player1, "Hill Giant");
        UUID opposingId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, opposingId);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(ownId);
        assertThat(choice.validPlayerIds()).isEmpty();
        harness.handlePermanentChosen(player1, ownId);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(findPermanent(player2, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void opponentsCreatureTargetingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hill Giant"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Hill Giant")).allSatisfy(giant ->
                assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void counterResolvesBeforePotentiallyLethalSpell() {
        harness.addToBattlefield(player1, new GraduationDay());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, 3, giantId);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hill Giant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Blaze");
    }
}
