package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddenLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability gives target creature +2/+1")
    void grantedAbilityBoostsTargetCreature() {
        Permanent forest = attachAura(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Grizzly Bears".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted ability can target an opponent's creature")
    void grantedAbilityBoostsOpponentsCreature() {
        Permanent forest = attachAura(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> "Grizzly Bears".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        attachAura(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Grizzly Bears".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent attachAura(final Player player) {
        harness.addToBattlefield(player, new Forest());
        Permanent forest = gd.playerBattlefields.get(player.getId()).getFirst();
        Permanent aura = new Permanent(new ForbiddenLore());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return forest;
    }
}
