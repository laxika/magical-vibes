package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Earthblighter.class, Forest.class, GrizzlyBears.class, RagingGoblin.class,
        BoggartShenanigans.class})
class EarthblighterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a Goblin to destroy a target land")
    void sacrificesGoblinToDestroyTargetLand() {
        Permanent earthblighter = addReadyEarthblighter(player1);
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent remainingGoblin = addCreatureReady(player1, new RagingGoblin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, land.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(earthblighter.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remainingGoblin);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a Goblin to sacrifice")
    void requiresGoblinToSacrifice() {
        addReadyEarthblighter(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addReadyEarthblighter(player1);
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice a noncreature Goblin permanent")
    void canSacrificeNoncreatureGoblin() {
        addReadyEarthblighter(player1);
        harness.addToBattlefield(player1, new BoggartShenanigans());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A tapped Goblin can pay the sacrifice cost to destroy your own land")
    void canSacrificeTappedGoblinToDestroyOwnLand() {
        Permanent earthblighter = addReadyEarthblighter(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        goblin.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(earthblighter.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Goblin")
    void cannotSacrificeOpponentsGoblin() {
        addReadyEarthblighter(player1);
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Earthblighter());
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        addReadyEarthblighter(player1).tap();
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Three generic mana cannot replace the required black mana")
    void requiresBlackMana() {
        addReadyEarthblighter(player1);
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Two mana are insufficient to activate")
    void requiresThreeMana() {
        addReadyEarthblighter(player1);
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A non-Goblin creature cannot pay the sacrifice cost")
    void cannotSacrificeNonGoblinCreature() {
        addReadyEarthblighter(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private Permanent addReadyEarthblighter(Player player) {
        return addCreatureReady(player, new Earthblighter());
    }
}
