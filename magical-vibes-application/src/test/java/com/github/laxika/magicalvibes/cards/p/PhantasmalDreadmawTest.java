package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantasmalDreadmaw.class, Shock.class, IcyManipulator.class})
class PhantasmalDreadmawTest extends BaseCardTest {

    @Test
    @DisplayName("Phantasmal Dreadmaw sacrifices itself when targeted by a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new PhantasmalDreadmaw());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, dreadmaw.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dreadmaw");
        harness.assertInGraveyard(player1, "Phantasmal Dreadmaw");
    }

    @Test
    @DisplayName("Phantasmal Dreadmaw sacrifices itself when targeted by an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new PhantasmalDreadmaw());

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player2, "Icy Manipulator");
        icyManipulator.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator),
                null, dreadmaw.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dreadmaw");
        harness.assertInGraveyard(player1, "Phantasmal Dreadmaw");
    }
}
