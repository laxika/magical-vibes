package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConformerShuriken.class, GrizzlyBears.class, HillGiant.class})
class ConformerShurikenTest extends BaseCardTest {

    @Test
    void tapsDefendingCreatureAndAddsPowerDifferenceCounters() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new HillGiant());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void addsCountersEvenWhenTargetIsAlreadyTapped() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.tap();

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotAddCountersWhenTargetIsNotLarger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipAttachesToCreatureYouControl() {
        Permanent shuriken = addShuriken(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, shuriken), null, creature.getId());
        harness.passBothPriorities();

        assertThat(shuriken.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addShuriken(Player player) {
        Permanent shuriken = new Permanent(new ConformerShuriken());
        shuriken.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shuriken);
        return shuriken;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
