package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonlordKolaghan.class, GrizzlyBears.class, JaceBeleren.class})
class DragonlordKolaghanTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have haste")
    void grantsHasteToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new DragonlordKolaghan());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A matching creature spell makes its caster lose 10 life")
    void matchingCreatureSpellMakesCasterLoseTenLife() {
        setUpOpponentTurn();
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A matching planeswalker spell makes its caster lose 10 life")
    void matchingPlaneswalkerSpellMakesCasterLoseTenLife() {
        setUpOpponentTurn();
        harness.setGraveyard(player2, List.of(new JaceBeleren()));
        harness.setHand(player2, List.of(new JaceBeleren()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A spell without a same-name card in its caster's graveyard does not trigger")
    void differentNameDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setGraveyard(player2, List.of(new DragonlordKolaghan()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new DragonlordKolaghan());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
