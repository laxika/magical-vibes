package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.h.HanaKami;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SenseiGoldenTail.class, IsamaruHoundOfKonda.class, EiganjoCastle.class, HanaKami.class})
class SenseiGoldenTailTest extends BaseCardTest {

    @Test
    @DisplayName("Training ability gives a creature a training counter, Samurai subtype, and Bushido")
    void trainsCreature() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        assertThat(sensei.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAINING)).isEqualTo(1);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.SAMURAI);

        declareAttackersAndPrepareBlockers(List.of(indexOf(target)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(target))));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each training ability activation adds another Bushido instance")
    void repeatedTrainingAddsBushidoInstances() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();
        sensei.untap();
        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(indexOf(target)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(target))));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAINING)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trained creature gets Bushido when it blocks")
    void trainedCreatureGetsBushidoWhenBlocking() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(sensei), null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(target), 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sensei Golden-Tail gets +1/+1 from its own Bushido when it blocks")
    void ownBushidoWhenBlocking() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        addCreatureReady(player2, new HanaKami());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(sensei), 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sensei)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sensei)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sensei Golden-Tail gets +1/+1 from its own Bushido when it becomes blocked")
    void ownBushidoWhenBecomesBlocked() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        addCreatureReady(player2, new HanaKami());

        declareAttackersAndPrepareBlockers(List.of(indexOf(sensei)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, indexOf(sensei))));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sensei)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sensei)).isEqualTo(2);
    }

    @Test
    @DisplayName("Training targets creatures only")
    void cannotTargetNonCreature() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sensei), null, castle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(sensei.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Training can be activated only at sorcery speed")
    void onlyActivatesAtSorcerySpeed() {
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sensei), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(sensei.isTapped()).isFalse();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
