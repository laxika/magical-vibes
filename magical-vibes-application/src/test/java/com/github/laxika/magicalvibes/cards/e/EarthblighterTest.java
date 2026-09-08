package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({Earthblighter.class, Forest.class, GrizzlyBears.class, RagingGoblin.class})
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

    private Permanent addReadyEarthblighter(Player player) {
        return addCreatureReady(player, new Earthblighter());
    }
}
