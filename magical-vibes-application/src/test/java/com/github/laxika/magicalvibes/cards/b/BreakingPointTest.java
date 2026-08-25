package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakingPoint.class, DrudgeSkeletons.class, GrizzlyBears.class})
class BreakingPointTest extends BaseCardTest {

    @Test
    @DisplayName("A player accepting takes 6 damage and prevents the destruction")
    void acceptingDamagePreventsDestruction() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("All players declining destroys all creatures")
    void allPlayersDecliningDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The first accepting player stops the remaining choices")
    void firstAcceptanceStopsChoices() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures destroyed by the decline branch cannot be regenerated")
    void destructionCannotBeRegenerated() {
        var skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    private void castBreakingPoint() {
        harness.setHand(player1, java.util.List.of(new BreakingPoint()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
