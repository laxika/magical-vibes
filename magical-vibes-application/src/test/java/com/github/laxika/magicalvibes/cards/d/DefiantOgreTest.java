package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefiantOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Defiant Ogre")
    void counterModePutsCounterOnItself() {
        castOgre(0, null);

        Permanent ogre = findPermanent(player1, "Defiant Ogre");
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(6);
    }

    @Test
    @DisplayName("Destroy mode destroys the target artifact")
    void destroyModeDestroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castOgre(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroy mode rejects a nonartifact target")
    void destroyModeRejectsNonartifactTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DefiantOgre()));
        addOgreMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void castOgre(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DefiantOgre()));
        addOgreMana();
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addOgreMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
