package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.cards.w.WakestoneGargoyle;
import com.github.laxika.magicalvibes.cards.w.Windreaver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProperBurial.class, SealOfDoom.class, WakestoneGargoyle.class, Windreaver.class})
class ProperBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to the toughness of a dying creature they control")
    void gainsLifeEqualToDyingCreatureToughness() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player1, new SealOfDoom());
        harness.addToBattlefield(player1, new WakestoneGargoyle());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player1, "Wakestone Gargoyle"));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerOnOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player1, new SealOfDoom());
        harness.addToBattlefield(player2, new WakestoneGargoyle());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player2, "Wakestone Gargoyle"));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Uses the creature's switched toughness immediately before death")
    void gainsLifeBasedOnSwitchedToughness() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player1, new SealOfDoom());
        harness.addToBattlefield(player1, new Windreaver());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 2, 2, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player1, "Windreaver"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Windreaver");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Includes a resolved toughness boost in the life gained")
    void gainsLifeBasedOnBoostedToughness() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player1, new SealOfDoom());
        harness.addToBattlefield(player1, new Windreaver());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 2, 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player1, "Windreaver"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Windreaver");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Returning a creature to hand does not trigger life gain")
    void doesNotTriggerWhenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new ProperBurial());
        harness.addToBattlefield(player1, new Windreaver());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 3, null, null);
        resolveAllTriggers();

        harness.assertInHand(player1, "Windreaver");
        harness.assertNotOnBattlefield(player1, "Windreaver");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
