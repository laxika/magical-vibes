package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrakDragonclaw.class, Cancel.class, GrizzlyBears.class, MightOfOaks.class, ForceAway.class})
class SurrakDragonclawTest extends BaseCardTest {

    @Test
    @DisplayName("Surrak Dragonclaw cannot be countered")
    void thisSpellCannotBeCountered() {
        SurrakDragonclaw surrak = new SurrakDragonclaw();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, surrak, "{2}{G}{U}{R}");
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

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, bears, "{1}{G}");
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

    @Test
    @DisplayName("Opponents' creature spells can still be countered")
    void doesNotProtectOpposingCreatureSpells() {
        harness.addToBattlefield(player1, new SurrakDragonclaw());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    @DisplayName("Surrak can be cast during an opponent's combat")
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castFromHand(player1, new SurrakDragonclaw(), "{2}{G}{U}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Surrak Dragonclaw");
    }

    @Test
    @DisplayName("Leaving the battlefield ends protection for a pending creature spell and removes trample")
    void leavingBattlefieldEndsContinuousAbilities() {
        Permanent surrak = harness.addToBattlefieldAndReturn(player1, new SurrakDragonclaw());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears spell = new GrizzlyBears();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(new ForceAway(), new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        harness.castAndResolveInstant(player2, 0, surrak.getId());

        harness.assertInHand(player1, "Surrak Dragonclaw");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
