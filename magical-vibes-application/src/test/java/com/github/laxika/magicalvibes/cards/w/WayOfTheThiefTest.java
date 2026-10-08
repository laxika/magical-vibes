package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GutterSkulk;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WayOfTheThief.class, GutterSkulk.class, SimicGuildgate.class})
class WayOfTheThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GutterSkulk());

        attachWayOfTheThief(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("A Gate makes the enchanted creature unblockable")
    void gateMakesEnchantedCreatureUnblockable() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        Permanent bears = addCreatureReady(player1, new GutterSkulk());

        attachWayOfTheThief(bears);

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Gate does not make the enchanted creature unblockable")
    void opponentGateDoesNotEnableEvasion() {
        Permanent bears = addCreatureReady(player1, new GutterSkulk());
        attachWayOfTheThief(bears);
        harness.addToBattlefield(player2, new SimicGuildgate());

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Way of the Thief can target only a creature")
    void cannotEnchantALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        // A legal creature target must exist somewhere, or the aura is unplayable before targeting
        // is ever validated (CR 601.2c).
        harness.addToBattlefield(player2, new GutterSkulk());
        harness.setHand(player1, List.of(new WayOfTheThief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void resolvesOnOpponentsCreatureAndUsesAuraControllersGate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GutterSkulk());
        harness.addToBattlefield(player2, new SimicGuildgate());
        harness.setHand(player1, List.of(new WayOfTheThief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Way of the Thief").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();

        harness.addToBattlefield(player1, new SimicGuildgate());
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    void onlyEnchantedCreatureBenefitsFromAura() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GutterSkulk());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GutterSkulk());
        attachWayOfTheThief(enchanted);
        harness.addToBattlefield(player1, new SimicGuildgate());

        assertThat(gqs.hasCantBeBlocked(gd, enchanted)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, other)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void losingOneOfTwoGatesKeepsEvasionAndLosingLastGateKeepsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GutterSkulk());
        attachWayOfTheThief(creature);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    private void attachWayOfTheThief(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WayOfTheThief());
        aura.setAttachedTo(creature.getId());
    }
}
