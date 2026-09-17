package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfPain.class, DawnElemental.class, ManorGargoyle.class, TreetopScout.class,
        TempleOfTheFalseGod.class})
class DecreeOfPainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and draws one card for each creature actually destroyed")
    void destroysAllCreaturesAndDrawsForEachDestroyed() {
        harness.addToBattlefield(player1, new TreetopScout());
        harness.addToBattlefield(player2, new TreetopScout());
        harness.addToBattlefield(player2, new ManorGargoyle());
        harness.setLibrary(player1, List.of(new DawnElemental(), new DawnElemental()));
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Treetop Scout");
        harness.assertNotOnBattlefield(player2, "Treetop Scout");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counts only creatures actually destroyed and ignores regeneration shields")
    void countsOnlyActuallyDestroyedCreatures() {
        Permanent creatureWithRegenerationShield = harness.addToBattlefieldAndReturn(player1, new TreetopScout());
        creatureWithRegenerationShield.setRegenerationShield(1);
        harness.addToBattlefield(player2, new TreetopScout());
        harness.addToBattlefield(player2, new ManorGargoyle());
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.setLibrary(player1, List.of(new DawnElemental(), new DawnElemental(), new DawnElemental()));
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Treetop Scout");
        harness.assertNotOnBattlefield(player2, "Treetop Scout");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
        harness.assertOnBattlefield(player1, "Temple of the False God");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cycling gives all creatures -2/-2 until end of turn and draws a card")
    void cyclingDebuffsAllCreaturesAndDraws() {
        Permanent ownElemental = harness.addToBattlefieldAndReturn(player1, new DawnElemental());
        Permanent opposingElemental = harness.addToBattlefieldAndReturn(player2, new DawnElemental());
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.setLibrary(player1, List.of(new TreetopScout()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownElemental)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingElemental)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Decree of Pain");
        harness.assertInHand(player1, "Treetop Scout");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownElemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownElemental)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingElemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingElemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cycling does not debuff creatures that enter after the trigger resolves")
    void cyclingDoesNotAffectCreaturesEnteringLater() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new DawnElemental());
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.setLibrary(player1, List.of(new TreetopScout()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new DawnElemental());
        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(3);
    }
}
