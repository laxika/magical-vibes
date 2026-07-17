package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LifeforceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target black spell")
    void countersBlackSpell() {
        harness.addToBattlefield(player1, new Lifeforce());
        harness.addMana(player1, ManaColor.GREEN, 2);

        ScatheZombies zombies = new ScatheZombies();
        harness.setHand(player2, List.of(zombies));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, zombies.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Scathe Zombies"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter a non-black spell")
    void cannotTargetNonBlackSpell() {
        harness.addToBattlefield(player1, new Lifeforce());
        harness.addMana(player1, ManaColor.GREEN, 2);

        HillGiant giant = new HillGiant();
        harness.setHand(player2, List.of(giant));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
