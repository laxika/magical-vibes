package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WitheringCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Withering Curse has a single conditional SPELL effect")
    void hasCorrectStructure() {
        WitheringCurse card = new WitheringCurse();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ConditionalReplacementEffect.class);
    }

    @Test
    @DisplayName("Without life gained, all creatures get -2/-2 (kills the 2/2, spares the 3/3)")
    void withoutLifeGainAppliesMinusTwoMinusTwo() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("If you gained life this turn, destroys all creatures instead (kills the 3/3 too)")
    void withLifeGainDestroysAllCreatures() {
        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(giant);

        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }
}
