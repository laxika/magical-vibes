package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesperatePlea.class, GrizzlyBears.class, HillGiant.class, Mortivore.class})
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

    @Test
    void stillDestroysCreatureWhenTheReturnPowerConditionIsNotMet() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Card returned = new HillGiant();
        Permanent destroyed = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(returned));

        cast(new int[]{0, 1}, List.of(returned.getId(), destroyed.getId()), sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destroyed);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(destroyed.getCard());
    }

    @Test
    void reanimatesCreatureWithPowerEqualToTheSacrificedCreature() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        cast(new int[]{0}, List.of(target.getId()), sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    void usesCharacteristicDefinedPowerInTheGraveyard() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Card target = new Mortivore();
        harness.setGraveyard(player1, List.of(target, new GrizzlyBears(), new GrizzlyBears()));

        cast(new int[]{0}, List.of(target.getId()), sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    void reanimatesCharacteristicDefinedCreatureWhenItsCurrentPowerIsSmallEnough() {
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        Card target = new Mortivore();
        harness.setGraveyard(player1, List.of(target));

        cast(new int[]{0}, List.of(target.getId()), sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    void rejectsAnExtraCombinedModeThatWouldDestroyTwoCreatures() {
        Permanent sacrifice = addCreatureReady(player1, new HillGiant());
        Card returned = new GrizzlyBears();
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(returned));

        assertThatThrownBy(() -> cast(new int[]{1, 2},
                List.of(first.getId(), returned.getId(), second.getId()), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
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
