package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowblindTest extends BaseCardTest {

    /** A 2/2 owned by {@code creatureController}, enchanted by a Snowblind player1 controls. */
    private Permanent enchantedBears(Player creatureController) {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(creatureController.getId()).add(bears);

        Permanent aura = new Permanent(new Snowblind());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return bears;
    }

    private void addSnowLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent snowLand = new Permanent(new Plains());
            TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
            gd.playerBattlefields.get(player.getId()).add(snowLand);
        }
    }

    @Test
    @DisplayName("Not attacking: X counts snow lands the enchanted creature's controller controls")
    void notAttackingCountsCreatureControllersSnowLands() {
        Permanent bears = enchantedBears(player2);
        addSnowLands(player2, 1);
        // The aura's controller has snow lands too; they must not be counted.
        addSnowLands(player1, 3);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Y is capped at toughness minus 1, so the creature survives")
    void toughnessNeverDropsBelowOne() {
        Permanent bears = enchantedBears(player2);
        addSnowLands(player2, 3);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking: X counts snow lands the defending player controls")
    void attackingCountsDefendingPlayersSnowLands() {
        Permanent bears = enchantedBears(player1);
        bears.setAttacking(true);
        bears.setAttackTarget(player2.getId());
        addSnowLands(player2, 2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking: the controller's own snow lands are ignored")
    void attackingIgnoresControllersSnowLands() {
        Permanent bears = enchantedBears(player1);
        bears.setAttacking(true);
        bears.setAttackTarget(player2.getId());
        addSnowLands(player1, 2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-snow lands are not counted")
    void nonSnowLandsAreNotCounted() {
        Permanent bears = enchantedBears(player2);
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff ends when Snowblind leaves the battlefield")
    void debuffEndsWhenAuraLeaves() {
        Permanent bears = enchantedBears(player2);
        addSnowLands(player2, 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Snowblind"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Snowblind()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
