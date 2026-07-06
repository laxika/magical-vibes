package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UlvenwaldBearTest extends BaseCardTest {

    

    @Test
    @DisplayName("No ETB trigger without morbid")
    void noEffectWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlvenwaldBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Morbid met — target creature gets two +1/+1 counters")
    void morbidPutsTwoCountersOnTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlvenwaldBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Morbid met — can put counters on own creature")
    void morbidPutsCountersOnOwnCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlvenwaldBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Killing a creature with Shock enables morbid ETB")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player2, bears1);
        harness.addToBattlefield(player2, bears2);
        harness.setHand(player1, List.of(new Shock(), new UlvenwaldBear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bears1Id = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bears1Id);
        harness.passBothPriorities();

        UUID bears2Id = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, bears2Id);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bears2Id))
                .findFirst().orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlvenwaldBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target and enters battlefield normally")
    void canCastWithoutTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UlvenwaldBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ulvenwald Bear"));
        assertThat(gd.stack).isEmpty();
    }
}
