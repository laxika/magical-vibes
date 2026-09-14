package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatsunariToadRider.class, Pacifism.class, GrizzlyBears.class, WindDrake.class})
class TatsunariToadRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment creates a legendary black and green 3/3 Frog named Keimi")
    void castingEnchantmentCreatesKeimi() {
        Permanent keimi = createKeimi();

        assertThat(keimi.getCard().isToken()).isTrue();
        assertThat(keimi.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(keimi.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(keimi.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
        assertThat(gqs.getEffectivePower(gd, keimi)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keimi)).isEqualTo(3);
    }

    @Test
    @DisplayName("Keimi drains each opponent and gains its controller life for later enchantments")
    void keimiTriggersForLaterEnchantment() {
        createKeimi();

        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(countPermanents(player1, "Keimi")).isEqualTo(1);
    }

    @Test
    @DisplayName("Tatsunari's ability makes Tatsunari and a Frog unblockable except by flying or reach")
    void abilityRestrictsBlockersForTatsunariAndFrog() {
        Permanent keimi = createKeimi();
        Permanent tatsunari = findPermanent(player1, "Tatsunari, Toad Rider");
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        harness.passBothPriorities();

        tatsunari.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying or reach");
    }

    @Test
    @DisplayName("The ability allows a creature with flying to block")
    void flyingCreatureCanBlock() {
        Permanent keimi = createKeimi();
        Permanent tatsunari = findPermanent(player1, "Tatsunari, Toad Rider");
        Permanent flier = addCreatureReady(player2, new WindDrake());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        harness.passBothPriorities();

        tatsunari.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(flier), 0)));

        assertThat(flier.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability only targets a Frog controlled by its activator")
    void abilityCannotTargetNonFrog() {
        harness.addToBattlefield(player1, new TatsunariToadRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Frog you control");
    }

    private Permanent createKeimi() {
        harness.addToBattlefield(player1, new TatsunariToadRider());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Keimi");
    }
}
