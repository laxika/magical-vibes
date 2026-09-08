package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DawnglareInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all creatures the target player controls")
    void tapsAllTargetPlayersCreatures() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new DawnglareInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(targetBear.isTapped()).isTrue();
        assertThat(targetGiant.isTapped()).isTrue();
        assertThat(targetArtifact.isTapped()).isFalse();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DawnglareInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DawnglareInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
