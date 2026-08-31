package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBurrows.class, GoblinPiker.class, GrizzlyBears.class, Forest.class})
class GoblinBurrowsTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        Permanent burrows = addReadyBurrows(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(burrows.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}{R}, {T}: Target Goblin creature gets +2/+0 until end of turn")
    void boostsTargetGoblinCreature() {
        Permanent burrows = addReadyBurrows(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getEffectivePower()).isEqualTo(4);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        assertThat(burrows.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Goblin Burrows' boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadyBurrows(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, goblin.getId());
        harness.passBothPriorities();
        assertThat(goblin.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin.getEffectivePower()).isEqualTo(2);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Burrows cannot target a non-Goblin creature")
    void cannotTargetNonGoblinCreature() {
        addReadyBurrows(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBurrows(Player player) {
        Permanent perm = new Permanent(new GoblinBurrows());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
