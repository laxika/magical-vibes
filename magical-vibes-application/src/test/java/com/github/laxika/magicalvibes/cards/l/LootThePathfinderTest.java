package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Loot, the Pathfinder")
@CardUsed({LootThePathfinder.class, Forest.class})
class LootThePathfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Green exhaust ability adds three mana of the chosen color")
    void greenExhaustAddsThreeMana() {
        Permanent loot = addReadyLoot();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(loot.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blue exhaust ability draws three cards")
    void blueExhaustDrawsThreeCards() {
        addReadyLoot();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Red exhaust ability deals three damage to any target")
    void redExhaustDealsThreeDamage() {
        addReadyLoot();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Each exhaust ability can be activated only once")
    void eachExhaustAbilityCanBeActivatedOnlyOnce() {
        Permanent loot = addReadyLoot();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        loot.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Red exhaust ability cannot target a land")
    void redExhaustCannotTargetLand() {
        addReadyLoot();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blueExhaustTapsLootAsAnActivationCost() {
        Permanent loot = addReadyLoot();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(loot.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void redExhaustTapsLootAsAnActivationCost() {
        Permanent loot = addReadyLoot();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());

        assertThat(loot.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void blueExhaustCannotBeActivatedWhileLootIsTapped() {
        Permanent loot = addReadyLoot();
        loot.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void redExhaustCannotBeActivatedWhileLootIsTapped() {
        Permanent loot = addReadyLoot();
        loot.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exhaustAbilitiesHaveIndependentOnceOnlyLimits() {
        Permanent loot = addReadyLoot();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        loot.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        loot.untap();
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertLife(player2, 17);
        loot.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void redExhaustCanDamageACreature() {
        addReadyLoot();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LootThePathfinder());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Loot, the Pathfinder");
    }

    @Test
    void hasteAllowsGreenExhaustOnTheTurnLootEnters() {
        Permanent loot = harness.addToBattlefieldAndReturn(player1, new LootThePathfinder());
        loot.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(loot.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLoot() {
        Permanent loot = harness.addToBattlefieldAndReturn(player1, new LootThePathfinder());
        loot.setSummoningSick(false);
        return loot;
    }
}
