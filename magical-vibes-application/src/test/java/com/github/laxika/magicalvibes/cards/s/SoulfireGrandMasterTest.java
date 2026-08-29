package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornetSting;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulfireGrandMaster.class, GrizzlyBears.class, HornetSting.class, Shock.class})
class SoulfireGrandMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells of any color you control have lifelink")
    void spellsOfAnyColorHaveLifelink() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new HornetSting()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Activated ability returns the next hand-cast instant or sorcery to hand")
    void returnsNextHandCastSpellToHand() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Return-to-hand rider waits past a non-instant or non-sorcery spell")
    void waitsForInstantOrSorcerySpell() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }
}
