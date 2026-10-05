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

        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator),
                null, dreadmaw.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dreadmaw");
        harness.assertInGraveyard(player1, "Phantasmal Dreadmaw");
    }

    @Test
    @DisplayName("Its controller's spell also triggers sacrifice, which resolves before the spell")
    void sacrificesWhenTargetedByOwnSpell() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new PhantasmalDreadmaw());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, dreadmaw.getId());

        harness.assertOnBattlefield(player1, "Phantasmal Dreadmaw");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dreadmaw");
        harness.assertInGraveyard(player1, "Phantasmal Dreadmaw");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Targeting a player does not trigger Phantasmal Dreadmaw")
    void doesNotSacrificeWhenSpellTargetsPlayer() {
        harness.addToBattlefield(player1, new PhantasmalDreadmaw());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Dreadmaw");
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its controller's activated ability also triggers sacrifice")
    void sacrificesWhenTargetedByOwnAbility() {
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new PhantasmalDreadmaw());
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icyManipulator),
                null, dreadmaw.getId());

        harness.assertOnBattlefield(player1, "Phantasmal Dreadmaw");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dreadmaw");
        harness.assertInGraveyard(player1, "Phantasmal Dreadmaw");
        harness.assertOnBattlefield(player1, "Icy Manipulator");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
