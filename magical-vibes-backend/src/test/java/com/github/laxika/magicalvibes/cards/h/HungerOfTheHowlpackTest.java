package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.MorbidReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HungerOfTheHowlpackTest extends BaseCardTest {

    @Test
    @DisplayName("Has morbid replacement from one counter to three counters")
    void hasCorrectStructure() {
        HungerOfTheHowlpack card = new HungerOfTheHowlpack();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(MorbidReplacementEffect.class);

        MorbidReplacementEffect effect =
                (MorbidReplacementEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.baseEffect()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        assertThat(effect.morbidEffect()).isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
        assertThat(((PutPlusOnePlusOneCounterOnTargetCreatureEffect) effect.baseEffect()).count()).isEqualTo(1);
        assertThat(((PutPlusOnePlusOneCounterOnTargetCreatureEffect) effect.morbidEffect()).count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Puts one +1/+1 counter on target creature without morbid")
    void putsOneCounterWithoutMorbid() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new HungerOfTheHowlpack()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts three +1/+1 counters on target creature with morbid")
    void putsThreeCountersWithMorbid() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new HungerOfTheHowlpack()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Morbid is checked at resolution time")
    void morbidCheckedAtResolution() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new HungerOfTheHowlpack()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.passBothPriorities();

        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1);
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new HungerOfTheHowlpack()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
