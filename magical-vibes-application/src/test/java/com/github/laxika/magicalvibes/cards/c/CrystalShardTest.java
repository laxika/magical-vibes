package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalShard.class, AlphaMyr.class, Forest.class})
class CrystalShardTest extends BaseCardTest {

    @Test
    @DisplayName("The target controller declines and the creature returns to its owner's hand")
    void controllerDeclinesCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new CrystalShard());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInHand(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("The target controller pays {1} and the creature stays on the battlefield")
    void controllerPaysCreatureStays() {
        harness.addToBattlefield(player1, new CrystalShard());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInHand(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("A controller without {1} cannot keep the creature")
    void cannotPayCreatureReturnsAutomatically() {
        harness.addToBattlefield(player1, new CrystalShard());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInHand(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("The ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeaves() {
        harness.addToBattlefield(player1, new CrystalShard());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability returns a controlled creature to its owner's hand")
    void returnsControlledCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new CrystalShard());
        AlphaMyr targetCard = new AlphaMyr();
        targetCard.setOwnerId(player1.getId());
        Permanent target = addCreatureReady(player2, targetCard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInHand(player1, "Alpha Myr");
        harness.assertNotInHand(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("The shard cannot be activated again while it is tapped")
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new CrystalShard());
        Permanent target = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.passBothPriorities();
        harness.assertInHand(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new CrystalShard());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
