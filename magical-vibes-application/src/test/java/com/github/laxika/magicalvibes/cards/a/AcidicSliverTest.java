package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AcidicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers, including opposing ones, gain the damage ability")
    void grantsAbilityToAllSlivers() {
        Permanent acidicSliver = addCreatureReady(player1, new AcidicSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, acidicSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Activating the granted ability sacrifices the Sliver and deals 2 damage to a player")
    void sacrificesSliverAndDamagesPlayer() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setLife(player2, 20);

        activateGrantedAbility(1, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownSliver);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activating the granted ability deals 2 damage to a creature")
    void damagesCreature() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activateGrantedAbility(1, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownSliver);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
    }

    private void activateGrantedAbility(int permanentIndex, UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, permanentIndex, 0, null, targetId);
        harness.passBothPriorities();
    }
}
