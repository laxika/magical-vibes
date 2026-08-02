package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FoundryChampionTest extends BaseCardTest {

    private void castChampion(UUID targetId) {
        harness.setHand(player1, List.of(new FoundryChampion()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB deals damage to target player equal to the number of creatures the controller controls, counting itself")
    void etbDamagesPlayerForCreatureCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        castChampion(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("ETB deals only 1 damage when the Champion is the controller's only creature")
    void etbDamagesForOneWhenAlone() {
        harness.setLife(player2, 20);

        castChampion(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB damage can kill a targeted creature")
    void etbKillsTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castChampion(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("{R} gives Foundry Champion +1/+0 until end of turn")
    void redAbilityBoostsPower() {
        harness.addToBattlefield(player1, new FoundryChampion());
        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(1);
        assertThat(champion.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{W} gives Foundry Champion +0/+1 until end of turn")
    void whiteAbilityBoostsToughness() {
        harness.addToBattlefield(player1, new FoundryChampion());
        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(0);
        assertThat(champion.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump abilities wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FoundryChampion());
        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(1);
        assertThat(champion.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(champion.getPowerModifier()).isEqualTo(0);
        assertThat(champion.getToughnessModifier()).isEqualTo(0);
    }
}
