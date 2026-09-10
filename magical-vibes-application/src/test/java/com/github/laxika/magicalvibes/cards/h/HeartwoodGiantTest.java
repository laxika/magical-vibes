package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartwoodGiant.class, Forest.class, Island.class, ChandraNalaar.class})
class HeartwoodGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest deals 2 damage to target player")
    void dealsDamageToPlayer() {
        addCreatureReady(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrificing a Forest deals 2 damage to target planeswalker")
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a Forest to sacrifice")
    void cannotActivateWithoutForest() {
        harness.addToBattlefield(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Island());

        findPermanent(player1, "Heartwood Giant").setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Forest")
    void cannotSacrificeOpponentsForest() {
        addCreatureReady(player1, new HeartwoodGiant());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A creature is not a legal target")
    void cannotTargetCreature() {
        addCreatureReady(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new HeartwoodGiant());

        UUID creatureId = findPermanent(player2, "Heartwood Giant").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
