package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoulderbranchGolem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianSprite.class, BoulderbranchGolem.class})
class ArgothianSpriteTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByArtifactCreature() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        sprite.setAttacking(true);
        addCreatureReady(player2, new BoulderbranchGolem());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByNonartifactCreature() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        sprite.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ArgothianSprite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void activatedAbilityPutsTwoPlusOnePlusOneCountersOnIt() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        sprite.setSummoningSick(true);
        sprite.tap();
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(sprite.isTapped()).isTrue();
    }

    @Test
    void repeatedActivationsAccumulateCountersOnlyOnTheirSource() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        Permanent other = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWithOnlySixMana() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sprite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNotPutCountersOnANewPermanentWhenSourceLeaves() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(sprite);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, sprite.getCard());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
