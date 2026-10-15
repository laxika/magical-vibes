package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMunitions;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreSledgeArsonist.class, MakeshiftMunitions.class, Spellbook.class, GrizzlyBears.class})
class PyreSledgeArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of your permanents sacrificed this turn")
    void dealsDamageForPermanentsSacrificedThisTurn() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbook.getId());
        harness.passBothPriorities();

        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.handlePermanentChosen(player1, grizzlyBears.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not count permanents sacrificed by an opponent")
    void doesNotCountOpponentsSacrifices() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addToBattlefield(player2, new MakeshiftMunitions());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can deal zero damage when no permanents were sacrificed")
    void dealsNoDamageWithoutSacrifices() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts sacrifices made after activation but before resolution")
    void countsSacrificesMadeInResponse() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbook.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Resolves after the Arsonist is sacrificed and counts that sacrifice")
    void countsItsOwnSacrificeInResponse() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.assertInGraveyard(player1, "Pyre-Sledge Arsonist");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can damage a creature target")
    void damagesCreatureTarget() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbook.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not count sacrifices from the previous turn")
    void resetsSacrificeCountOnNextTurn() {
        addCreatureReady(player1, new PyreSledgeArsonist());
        harness.addToBattlefield(player1, new MakeshiftMunitions());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbook.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
