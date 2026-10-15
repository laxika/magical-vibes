package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PuresteelPaladin;
import com.github.laxika.magicalvibes.cards.b.BastionProtector;
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

@CardUsed({ConformerShuriken.class, PuresteelPaladin.class, BastionProtector.class})
class ConformerShurikenTest extends BaseCardTest {

    @Test
    void tapsDefendingCreatureAndAddsPowerDifferenceCounters() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void addsCountersEvenWhenTargetIsAlreadyTapped() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());
        target.tap();

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotAddCountersWhenTargetIsNotLarger() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new PuresteelPaladin());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new PuresteelPaladin());
        addCreatureReady(player2, new PuresteelPaladin());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipAttachesToCreatureYouControl() {
        Permanent shuriken = addShuriken(player1);
        Permanent creature = addCreatureReady(player1, new PuresteelPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, shuriken), null, creature.getId());
        harness.passBothPriorities();

        assertThat(shuriken.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void countersStillGoOnOriginalAttackerAfterEquipmentMoves() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent otherCreature = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        shuriken.setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countersStillGoOnAttackerAfterEquipmentBecomesUnattached() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        shuriken.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackingCreatureControllerChoosesTargetWhenOpponentOwnsEquipment() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player2);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void usesNegativeAttackerPowerInDifference() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        attacker.setPowerModifier(-4);
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void comparesTwoNegativePowersWithoutClampingThem() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        attacker.setPowerModifier(-4);
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());
        target.setPowerModifier(-4);

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void usesPowersAtResolutionInsteadOfWhenAbilityTriggers() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        target.setPowerModifier(3);
        attacker.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotAddCountersWhenTargetLeavesBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new PuresteelPaladin());
        Permanent shuriken = addShuriken(player1);
        shuriken.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new BastionProtector());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addShuriken(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ConformerShuriken());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
