package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LeaveNoTrace;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.cards.s.SelesnyaSignet;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FollowedFootsteps.class, SiegeWurm.class, SelesnyaSignet.class,
        LeaveNoTrace.class, Putrefy.class})
class FollowedFootstepsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of the enchanted creature at your upkeep")
    void createsTokenCopyOfEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(5);
                    assertThat(token.getEffectiveToughness()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm")).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new SelesnyaSignet());
        harness.setHand(player1, List.of(new FollowedFootsteps()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The upkeep trigger still creates a copy after the Aura is destroyed")
    void createsCopyAfterAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new LeaveNoTrace()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Followed Footsteps"));
        harness.assertInGraveyard(player1, "Followed Footsteps");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm")).singleElement()
                .satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("No copy is created when the creature dies and the Aura becomes unattached")
    void doesNotCreateCopyAfterEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);
        advanceToUpkeep(player1);

        harness.setHand(player2, List.of(new Putrefy()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player2, "Siege Wurm");
        harness.assertInGraveyard(player1, "Followed Footsteps");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm")).isEmpty();
    }

    @Test
    @DisplayName("Copies do not inherit counters, damage, or tapped status")
    void copiesOnlyCopiableValues() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);
        advanceToUpkeep(player1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setMarkedDamage(1);
        creature.tap();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm")).singleElement()
                .satisfies(token -> {
                    assertThat(token.getPlusOnePlusOneCounters()).isZero();
                    assertThat(token.getMarkedDamage()).isZero();
                    assertThat(token.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
                });
    }

    private void castFollowedFootsteps(Permanent creature) {
        harness.setHand(player1, List.of(new FollowedFootsteps()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
