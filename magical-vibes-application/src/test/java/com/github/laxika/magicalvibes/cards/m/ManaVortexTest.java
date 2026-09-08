package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.s.ScavengerFolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaVortex.class, ScavengerFolk.class, CityOfShadows.class, ImprisonedInTheMoon.class})
class ManaVortexTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses which land to sacrifice on upkeep")
    void activePlayerChoosesLandOnUpkeep() {
        harness.addToBattlefield(player1, new ManaVortex());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());
        Permanent chosenLand = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosenLand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(firstLand.getId()))
                .noneMatch(permanent -> permanent.getId().equals(chosenLand.getId()));
        harness.assertOnBattlefield(player1, "Mana Vortex");
    }

    @Test
    @DisplayName("A permanent turned into a land prevents the no-lands state trigger")
    void doesNotSacrificeItselfWhileAnEffectiveLandExists() {
        harness.addToBattlefield(player1, new ManaVortex());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScavengerFolk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(creature.getId());
        assertThat(gqs.isLand(gd, creature)).isTrue();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Mana Vortex");
    }

    @Test
    @DisplayName("Declining the land sacrifice counters Mana Vortex")
    void decliningLandSacrificeCountersItself() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CityOfShadows());
        harness.addToBattlefield(player1, new ScavengerFolk());
        castManaVortex(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Mana Vortex");
        harness.assertNotOnBattlefield(player1, "Mana Vortex");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Counters itself on cast when its controller controls no land")
    void countersItselfWithoutLand() {
        harness.addToBattlefield(player1, new ScavengerFolk());
        castManaVortex(player1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mana Vortex");
        harness.assertNotOnBattlefield(player1, "Mana Vortex");
    }

    @Test
    @DisplayName("Sacrificing a land lets Mana Vortex resolve")
    void sacrificingLandLetsItResolve() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CityOfShadows());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScavengerFolk());
        castManaVortex(player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mana Vortex");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Each player's upkeep makes that player sacrifice a land")
    void activePlayerSacrificesLandOnUpkeep() {
        harness.addToBattlefield(player1, new ManaVortex());
        Permanent controllerLand = harness.addToBattlefieldAndReturn(player1, new CityOfShadows());
        Permanent activePlayerLand = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(activePlayerLand.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(controllerLand.getId()));
        harness.assertOnBattlefield(player1, "Mana Vortex");
    }

    @Test
    @DisplayName("Sacrifices itself after the last land leaves the battlefield")
    void sacrificesItselfWhenThereAreNoLands() {
        harness.addToBattlefield(player1, new ManaVortex());
        harness.addToBattlefield(player1, new CityOfShadows());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mana Vortex");
    }

    private void castManaVortex(Player player) {
        harness.castFromHand(player, new ManaVortex(), "{1}{U}{U}");
    }
}
