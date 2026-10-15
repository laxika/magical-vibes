package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelMyr.class, Shatter.class, GalvanicBlast.class, GraspOfDarkness.class})
class DarksteelMyrTest extends BaseCardTest {

    @Test
    void survivesDestroyArtifactSpell() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, myr.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotInGraveyard(player2, "Darksteel Myr");
        harness.assertInGraveyard(player1, "Shatter");
    }

    @Test
    void survivesLethalDamageWithoutPreventingIt() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, myr.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertNotInGraveyard(player2, "Darksteel Myr");
        assertThat(myr.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void diesWhenToughnessIsReducedBelowZero() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, myr.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Darksteel Myr");
        harness.assertInGraveyard(player2, "Darksteel Myr");
    }
}
