package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeaverOfHarmony.class, ProdigalPyromancer.class, TrialOfZeal.class, GrizzlyBears.class})
class WeaverOfHarmonyTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other enchantment creatures you control")
    void buffsOtherEnchantmentCreaturesYouControl() {
        harness.addToBattlefield(player1, new WeaverOfHarmony());
        harness.addToBattlefield(player1, new WeaverOfHarmony());

        List<Permanent> weavers = findPermanents(player1, "Weaver of Harmony");

        assertThat(weavers).hasSize(2);
        for (Permanent weaver : weavers) {
            assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, weaver)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Does not buff non-enchantment creatures")
    void doesNotBuffNonEnchantmentCreatures() {
        harness.addToBattlefield(player1, new WeaverOfHarmony());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff enchantment creatures an opponent controls")
    void doesNotBuffOpponentEnchantmentCreatures() {
        harness.addToBattlefield(player1, new WeaverOfHarmony());
        harness.addToBattlefield(player2, new WeaverOfHarmony());

        Permanent opponentWeaver = findPermanent(player2, "Weaver of Harmony");

        assertThat(gqs.getEffectivePower(gd, opponentWeaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentWeaver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Copies a triggered ability from an enchantment source")
    void copiesTriggeredAbilityFromEnchantmentSource() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WeaverOfHarmony());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, triggerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot target an ability from a non-enchantment source")
    void cannotTargetAbilityFromNonEnchantmentSource() {
        addCreatureReady(player1, new WeaverOfHarmony());
        addReadyPyromancer(player1);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pyromancerAbilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyPyromancer(Player player) {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
    }
}
