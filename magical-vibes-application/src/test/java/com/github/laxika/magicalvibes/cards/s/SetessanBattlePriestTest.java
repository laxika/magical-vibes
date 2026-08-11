package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SetessanBattlePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Setessan Battle Priest gains 2 life")
    void castingSpellThatTargetsPriestGainsLife() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID priestId = harness.getPermanentId(player1, "Setessan Battle Priest");
        harness.castInstant(player1, 0, priestId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Setessan Battle Priest")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's spell that targets Setessan Battle Priest does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID priestId = harness.getPermanentId(player1, "Setessan Battle Priest");
        harness.castInstant(player2, 0, priestId);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(20);
    }
}
