package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowInTheWarp.class, GrizzlyBears.class, MindStone.class})
class ShadowInTheWarpTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature spell each turn costs {2} less")
    void firstCreatureSpellCostsTwoLess() {
        harness.addToBattlefield(player1, new ShadowInTheWarp());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals 2 damage when an opponent casts their first noncreature spell")
    void damagesOpponentForFirstNoncreatureSpell() {
        harness.addToBattlefield(player1, new ShadowInTheWarp());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new MindStone(), new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for spells cast by its controller")
    void doesNotTriggerForControllerSpell() {
        harness.addToBattlefield(player1, new ShadowInTheWarp());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
