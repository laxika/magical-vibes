package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CagedZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without morbid")
    void cannotActivateWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent zombie = addReadyZombie(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int zombieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);

        assertThatThrownBy(() -> harness.activateAbility(player1, zombieIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Morbid");
    }

    @Test
    @DisplayName("Each opponent loses 2 life when morbid is met")
    void eachOpponentLosesTwoLifeWhenMorbidIsMet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent zombie = addReadyZombie(player1);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int zombieIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);
        harness.activateAbility(player1, zombieIndex, null, null);
        assertThat(zombie.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyZombie(Player player) {
        Permanent zombie = new Permanent(new CagedZombie());
        zombie.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(zombie);
        return zombie;
    }
}
