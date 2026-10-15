package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TheScarabGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodPharaohsFaithful.class, FugitiveWizard.class, ScatheZombies.class,
        HillGiant.class, SuntailHawk.class, GrizzlyBears.class, TheScarabGod.class})
class GodPharaohsFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a blue spell gains 1 life")
    void blueSpellGainsLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a black spell gains 1 life")
    void blackSpellGainsLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a red spell gains 1 life")
    void redSpellGainsLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a white spell does not gain life")
    void whiteSpellGainsNoLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Casting a green spell does not gain life")
    void greenSpellGainsNoLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's blue spell does not trigger Faithful")
    void opponentSpellGainsNoLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Each Faithful triggers separately for the same spell")
    void multipleFaithfulsEachGainLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("A blue creature entering without being cast does not trigger Faithful")
    void enteringWithoutCastingGainsNoLife() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addToBattlefield(player1, new FugitiveWizard());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("A spell with two qualifying colors gains only 1 life")
    void multicoloredSpellTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new GodPharaohsFaithful());
        harness.setHand(player1, List.of(new TheScarabGod()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.assertOnBattlefield(player1, "The Scarab God");
    }
}
