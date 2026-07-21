package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianasDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a black creature; no life lost when it isn't a Liliana")
    void destroysBlackCreatureWithoutLifeLoss() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID corpseId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, corpseId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroying a Liliana planeswalker makes its controller lose 3 life")
    void destroyingLilianaCostsControllerThreeLife() {
        harness.setLife(player2, 20);

        Permanent liliana = new Permanent(new LilianaDeathsMajesty());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(liliana);

        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, liliana.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana, Death's Majesty");
        harness.assertInGraveyard(player2, "Liliana, Death's Majesty");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        // A legal black target makes the spell castable; aiming at the green creature is rejected.
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LilianasDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }
}
