package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EstwaldShieldbasher;
import com.github.laxika.magicalvibes.cards.h.HallowedHaunting;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnhartGeist.class, HallowedHaunting.class, EstwaldShieldbasher.class, HerosDownfall.class})
class DawnhartGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life whenever you cast an enchantment spell")
    void gainsLifeOnEnchantmentCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());

        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void noLifeOnCreatureCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());

        harness.castFromHand(player1, new EstwaldShieldbasher(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment spell")
    void noLifeOnOpponentEnchantmentCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        harness.forceActivePlayer(player2);
        int startingLife = gd.getLife(player1.getId());

        harness.castFromHand(player2, new HallowedHaunting(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Life gain resolves before the enchantment spell")
    void gainsLifeBeforeEnchantmentResolves() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());

        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        harness.assertLife(player1, startingLife);
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        harness.assertNotOnBattlefield(player1, "Hallowed Haunting");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hallowed Haunting");
        harness.assertLife(player1, startingLife + 2);
    }

    @Test
    @DisplayName("Each Dawnhart Geist triggers independently")
    void multipleGeistsEachGainLife() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        harness.addToBattlefield(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());

        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 4);
        harness.assertNotOnBattlefield(player1, "Hallowed Haunting");
        harness.passBothPriorities();
        harness.assertLife(player1, startingLife + 4);
    }

    @Test
    @DisplayName("Triggers on every enchantment cast during the same turn")
    void triggersOnSuccessiveEnchantmentCasts() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());

        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, startingLife + 2);

        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not trigger life gain")
    void noLifeOnEnchantmentEnteringWithoutCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new HallowedHaunting());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, startingLife);
    }

    @Test
    @DisplayName("Removing Dawnhart Geist does not stop its pending life gain")
    void pendingTriggerSurvivesSourceRemoval() {
        var geist = harness.addToBattlefieldAndReturn(player1, new DawnhartGeist());
        int startingLife = gd.getLife(player1.getId());
        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        harness.setHand(player2, List.of(new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, geist.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Dawnhart Geist");
        harness.assertLife(player1, startingLife);
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hallowed Haunting");
    }
}
