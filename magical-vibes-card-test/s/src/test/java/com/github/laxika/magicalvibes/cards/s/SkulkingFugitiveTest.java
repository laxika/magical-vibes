package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SkulkingFugitive.class, GiantGrowth.class, ProdigalPyromancer.class, Counterspell.class})
class SkulkingFugitiveTest extends BaseCardTest {

    @Test
    @DisplayName("Skulking Fugitive is sacrificed when targeted by a spell")
    void sacrificedWhenTargetedBySpell() {
        Permanent fugitive = harness.addToBattlefieldAndReturn(player1, new SkulkingFugitive());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, fugitive.getId());

        harness.assertNotOnBattlefield(player1, "Skulking Fugitive");
        harness.assertInGraveyard(player1, "Skulking Fugitive");
    }

    @Test
    @DisplayName("Skulking Fugitive is not sacrificed when a spell targets another creature")
    void notSacrificedWhenAnotherCreatureIsTargeted() {
        harness.addToBattlefield(player1, new SkulkingFugitive());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());

        harness.assertOnBattlefield(player1, "Skulking Fugitive");
        harness.assertNotInGraveyard(player1, "Skulking Fugitive");
    }

    @Test
    @DisplayName("Skulking Fugitive is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent fugitive = harness.addToBattlefieldAndReturn(player1, new SkulkingFugitive());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer),
                null, fugitive.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Fugitive");
        harness.assertInGraveyard(player1, "Skulking Fugitive");
    }

    @Test
    @DisplayName("An opponent's spell triggers sacrifice only when the trigger resolves")
    void opponentSpellSacrificesOnTriggerResolution() {
        Permanent fugitive = harness.addToBattlefieldAndReturn(player1, new SkulkingFugitive());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, fugitive.getId());

        harness.assertOnBattlefield(player1, "Skulking Fugitive");
        harness.assertNotInGraveyard(player1, "Skulking Fugitive");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Fugitive");
        harness.assertInGraveyard(player1, "Skulking Fugitive");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Giant Growth");
    }

    @Test
    @DisplayName("Countering the targeting spell does not stop the sacrifice trigger")
    void sacrificeTriggerSurvivesCounteringTargetingSpell() {
        Permanent fugitive = harness.addToBattlefieldAndReturn(player1, new SkulkingFugitive());
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, fugitive.getId());

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, growth.getId());

        harness.assertInGraveyard(player1, "Giant Growth");
        harness.assertOnBattlefield(player1, "Skulking Fugitive");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skulking Fugitive");
        harness.assertInGraveyard(player1, "Skulking Fugitive");
    }
}
