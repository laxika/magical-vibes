package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SurrakDragonclawTest extends BaseCardTest {

    @Test
    @DisplayName("Surrak Dragonclaw cannot be countered")
    void thisSpellCannotBeCountered() {
        SurrakDragonclaw surrak = new SurrakDragonclaw();
        harness.setHand(player1, List.of(surrak));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, surrak.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Surrak Dragonclaw");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Creature spells you control cannot be countered")
    void protectsOwnCreatureSpells() {
        harness.addToBattlefield(player1, new SurrakDragonclaw());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Surrak Dragonclaw does not protect noncreature spells")
    void doesNotProtectNonCreatureSpells() {
        harness.addToBattlefield(player1, new SurrakDragonclaw());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Other creatures you control have trample")
    void grantsTrampleToOtherOwnCreatures() {
        Permanent surrak = harness.addToBattlefieldAndReturn(player1, new SurrakDragonclaw());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, surrak, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }
}
