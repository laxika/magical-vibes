package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatsOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature its controller controls")
    void destroysOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Destroys an artifact and a land its controller controls")
    void destroysOwnArtifactAndLand() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, thopter.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, swamp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(thopter, swamp);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefieldAndReturn(player1, new RatsOfRath());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enemy);
    }
}
