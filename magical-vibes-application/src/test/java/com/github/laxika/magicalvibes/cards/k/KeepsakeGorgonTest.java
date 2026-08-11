package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.XathridGorgon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeepsakeGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("When Keepsake Gorgon becomes monstrous, it destroys a chosen non-Gorgon creature an opponent controls")
    void becomingMonstrousDestroysChosenNonGorgonCreature() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gorgon);
    }

    @Test
    @DisplayName("Keepsake Gorgon's trigger is skipped when only a Gorgon is available")
    void triggerSkipsWhenOnlyGorgonIsAvailable() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gorgon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Keepsake Gorgon's monstrosity ability cannot be activated twice")
    void monstrosityOnlyResolvesOnce() {
        addReadyKeepsakeGorgon();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyKeepsakeGorgon() {
        Permanent keepsakeGorgon = harness.addToBattlefieldAndReturn(player1, new KeepsakeGorgon());
        keepsakeGorgon.setSummoningSick(false);
        return keepsakeGorgon;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
