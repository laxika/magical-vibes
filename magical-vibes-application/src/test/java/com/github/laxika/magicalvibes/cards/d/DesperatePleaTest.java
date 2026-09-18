package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperatePlea.class, GrizzlyBears.class, HillGiant.class})
class DesperatePleaTest extends BaseCardTest {

    @Test
    void reanimatesTargetCreatureWhosePowerDoesNotExceedTheSacrificedCreature() {
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        cast(new int[]{0}, List.of(target.getId()), sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    void doesNotReanimateCreatureWithGreaterPower() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Card target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));

        cast(new int[]{0}, List.of(target.getId()), sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(target, sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
    }

    @Test
    void destroysTargetCreature() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        cast(new int[]{1}, List.of(target.getId()), sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void choosingBothModesResolvesBothEffects() {
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        Card returned = new GrizzlyBears();
        Permanent destroyed = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(returned));

        cast(new int[]{0, 1}, List.of(returned.getId(), destroyed.getId()), sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destroyed);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, java.util.UUID sacrificeId) {
        harness.setHand(player1, List.of(new DesperatePlea()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModesAndSacrifice(
                player1, 0, 1, 2, modes, targetIds, sacrificeId);
        harness.passBothPriorities();
    }
}
