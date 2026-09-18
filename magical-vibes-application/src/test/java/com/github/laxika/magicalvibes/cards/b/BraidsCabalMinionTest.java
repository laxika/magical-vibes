package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CharmedPendant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidsCabalMinion.class, CharmedPendant.class, Forest.class, GroundSeal.class, Werebear.class})
class BraidsCabalMinionTest extends BaseCardTest {

    @Test
    @DisplayName("At each player's upkeep that player sacrifices an artifact, creature, or land")
    void activePlayerChoosesEligiblePermanentToSacrifice() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CharmedPendant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Werebear());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("A non-artifact, noncreature, nonland permanent is not eligible")
    void nonEligiblePermanentIsNotSacrificed() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GroundSeal());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Werebear());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("The controller also sacrifices a permanent during their own upkeep")
    void controllerIsAlsoAffected() {
        Permanent braids = harness.addToBattlefieldAndReturn(player1, new BraidsCabalMinion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Werebear());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(braids.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Does nothing when the active player controls no artifact, creature, or land")
    void noEligiblePermanentDoesNotPromptOrSacrifice() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GroundSeal());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enchantment.getId()));
    }
}
