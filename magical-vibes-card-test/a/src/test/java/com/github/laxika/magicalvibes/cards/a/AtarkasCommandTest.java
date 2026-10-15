package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtarkasCommand.class, Forest.class, GrizzlyBears.class, ZuranOrb.class})
class AtarkasCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to opponents and boosts your creatures with reach")
    void dealsDamageAndBoostsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtarkasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 3}, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Restricts opponents' life gain and puts a land from hand onto the battlefield")
    void restrictsOpponentsAndPutsLandOntoBattlefield() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AtarkasCommand(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2}, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Forest");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player2, 20);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void decliningLandPlacementStillResolvesCreatureMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtarkasCommand(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{2, 3}, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    void creatureModeOnlyAffectsOwnCreaturesPresentAtResolutionAndExpires() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtarkasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 3}, List.of());
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.REACH)).isTrue();
        for (Permanent unaffected : List.of(opponentCreature, laterCreature)) {
            assertThat(gqs.getEffectivePower(gd, unaffected)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, unaffected)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.REACH)).isFalse();
        }
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.REACH)).isFalse();
    }

    @Test
    void lifeGainRestrictionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AtarkasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @CardUsed({AtarkasCommand.class, Forest.class})
    void canPutLandOntoBattlefieldAfterUsingNormalLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest(), new AtarkasCommand(), new Forest()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2}, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(permanent -> !permanent.isTapped());
        harness.assertNotInHand(player1, "Forest");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @CardUsed({AtarkasCommand.class})
    void acceptingLandModeWithoutLandDoesNotPreventOtherMode() {
        harness.setHand(player1, List.of(new AtarkasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2}, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Atarka's Command");
        assertThat(gd.stack).isEmpty();
    }
}
