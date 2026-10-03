package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumingSinkhole.class, Mutavault.class, GrizzlyBears.class, ChandraNalaar.class})
class ConsumingSinkholeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target land creature")
    void exilesLandCreature() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        cast(0, mutavault.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mutavault);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(mutavault.getCard());
    }

    @Test
    @DisplayName("Deals 4 damage to a target player")
    void dealsDamageToPlayer() {
        cast(1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals 4 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        cast(1, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a non-land creature for the exile mode")
    void rejectsNonLandCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can deal 4 damage to its own controller")
    void dealsDamageToController() {
        cast(1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Rejects an unanimated land for the exile mode")
    void rejectsNonCreatureLand() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player2, new Mutavault());

        assertThatThrownBy(() -> cast(0, mutavault.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature for the damage mode")
    void rejectsCreatureForDamageMode() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles an opponent's animated land without dealing player damage")
    void exilesOpponentsLandCreature() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player2, new Mutavault());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        cast(0, mutavault.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mutavault);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(mutavault.getCard());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An exile spell does nothing when a response has already exiled its target")
    void doesNothingWhenTargetAlreadyExiled() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player2, new Mutavault());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        cast(0, mutavault.getId());
        cast(0, mutavault.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mutavault);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(mutavault.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof ConsumingSinkhole);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new ConsumingSinkhole()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mode, targetId);
    }
}
