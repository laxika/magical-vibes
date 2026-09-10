package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.w.WingedSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClotSliver.class, WingedSliver.class, LowlandGiant.class})
class ClotSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain the regeneration ability")
    void grantsAbilityToAllSlivers() {
        Permanent clotSliver = addCreatureReady(player1, new ClotSliver());
        Permanent ownSliver = addCreatureReady(player1, new WingedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new WingedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, clotSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new ClotSliver());
        Permanent nonSliver = addCreatureReady(player1, new LowlandGiant());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("Activating the granted ability grants a regeneration shield to that Sliver")
    void grantsRegenerationShield() {
        addCreatureReady(player1, new ClotSliver());
        Permanent otherSliver = addCreatureReady(player1, new WingedSliver());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(otherSliver.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @CardUsed(AmoeboidChangeling.class)
    @DisplayName("A Sliver that loses all creature types no longer has the ability")
    void losingSliverTypeRemovesAbility() {
        Permanent clotSliver = addCreatureReady(player1, new ClotSliver());
        Permanent amoeboid = addCreatureReady(player1, new AmoeboidChangeling());

        assertThat(gs.getEffectiveActivatedAbilities(gd, clotSliver)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 1, null, clotSliver.getId());
        harness.passBothPriorities();

        assertThat(gs.getEffectiveActivatedAbilities(gd, clotSliver)).isEmpty();
    }
}
