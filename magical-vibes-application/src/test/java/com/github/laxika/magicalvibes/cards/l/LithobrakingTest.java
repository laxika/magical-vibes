package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lithobraking.class, EntropicBattlecruiser.class, IntrepidTenderfoot.class, Forest.class})
class LithobrakingTest extends BaseCardTest {

    @Test
    void createsLanderAndSacrificingAnArtifactDealsDamageToEachCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        harness.addToBattlefield(player1, new IntrepidTenderfoot());
        harness.addToBattlefield(player2, new IntrepidTenderfoot());

        castLithobraking();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Entropic Battlecruiser");
        harness.assertNotOnBattlefield(player1, "Intrepid Tenderfoot");
        harness.assertNotOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void decliningArtifactSacrificeLeavesCreaturesAndCreatesLander() {
        harness.addToBattlefield(player1, new EntropicBattlecruiser());
        harness.addToBattlefield(player1, new IntrepidTenderfoot());
        harness.addToBattlefield(player2, new IntrepidTenderfoot());

        castLithobraking();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Entropic Battlecruiser");
        harness.assertOnBattlefield(player1, "Intrepid Tenderfoot");
        harness.assertOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void newlyCreatedLanderCanBeSacrificedAndDamageWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new IntrepidTenderfoot());
        harness.addToBattlefield(player2, new IntrepidTenderfoot());

        castLithobraking();
        harness.passBothPriorities();
        Permanent lander = findPermanent(player1, "Lander");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, lander.getId());

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        harness.assertInGraveyard(player1, "Lithobraking");
        harness.assertOnBattlefield(player1, "Intrepid Tenderfoot");
        harness.assertOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Intrepid Tenderfoot");
        harness.assertInGraveyard(player2, "Intrepid Tenderfoot");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void landerCanFindBasicLandTappedOnTheTurnItIsCreated() {
        castLithobraking();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Forest forest = new Forest();
        harness.setLibrary(player1, java.util.List.of(new Lithobraking(), forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(forest);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof Lithobraking);
    }

    @Test
    void landerCanBeSacrificedWithoutBasicLandToFind() {
        castLithobraking();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.setLibrary(player1, java.util.List.of(new Lithobraking()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageMarksExactlyTwoOnSurvivingCreaturesAndLeavesNoncreatureArtifacts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        creature.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new EntropicBattlecruiser());

        castLithobraking();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Lander").getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Intrepid Tenderfoot");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Entropic Battlecruiser");
        assertThat(findPermanent(player2, "Entropic Battlecruiser").getMarkedDamage()).isZero();
    }

    @Test
    void landerSearchMayFailToFindEvenWhenBasicLandIsAvailable() {
        castLithobraking();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Forest forest = new Forest();
        harness.setLibrary(player1, java.util.List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }
    private void castLithobraking() {
        harness.castFromHand(player1, new Lithobraking(), "{2}{R}");
    }
}
