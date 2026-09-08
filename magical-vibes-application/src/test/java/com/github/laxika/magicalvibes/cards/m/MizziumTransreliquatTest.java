package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MizziumTransreliquat.class, DarksteelCitadel.class, Manalith.class, GrizzlyBears.class})
class MizziumTransreliquatTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a copy of target artifact until end of turn")
    void becomesCopyOfTargetArtifactUntilEndOfTurn() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent citadel = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, citadel.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(transreliquat.getCard()).isSameAs(transreliquat.getOriginalCard());
    }

    @Test
    @DisplayName("The copy-with-exception ability remains available on the copy")
    void copyWithExceptionRetainsAbility() {
        Permanent transreliquat = harness.addToBattlefieldAndReturn(player1, new MizziumTransreliquat());
        Permanent citadel = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Permanent manalith = harness.addToBattlefieldAndReturn(player2, new Manalith());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, citadel.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, manalith.getId());
        harness.passBothPriorities();

        assertThat(transreliquat.getCard()).isNotSameAs(transreliquat.getOriginalCard());
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new MizziumTransreliquat());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
