package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GitrogHorrorOfZhava.class, GrizzlyBears.class, FieldOfRuin.class, Forest.class})
class GitrogHorrorOfZhavaTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Opponent may sacrifice nontoken creature to tap Gitrog and seek land")
    void opponentMaySacrificeNontokenCreatureToTapGitrogAndSeekLand() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new FieldOfRuin()));

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gitrog.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent soughtLand = findPermanent(player1, "Field of Ruin");
        assertThat(soughtLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped Gitrog does not offer combat choice")
    void tappedGitrogDoesNotOfferCombatChoice() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        gitrog.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Landfall perpetually grants the sacrifice draw ability")
    void landfallPerpetuallyGrantsTheSacrificeDrawAbility() {
        harness.addToBattlefield(player1, new GitrogHorrorOfZhava());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        // The granted ability, then the Forest's own mana ability (CR 305.6).
        assertThat(gs.getEffectiveActivatedAbilities(gd, forest)).hasSize(2)
                .endsWith(ManaAbilities.intrinsic(ManaColor.GREEN));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        forest.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land that entered as a copy gets the ability on its own card, which keeps it after leaving")
    @CardUsed(com.github.laxika.magicalvibes.cards.v.Vesuva.class)
    void landCopyGetsTheAbilityOnItsOwnCard() {
        harness.addToBattlefield(player1, new GitrogHorrorOfZhava());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        com.github.laxika.magicalvibes.cards.v.Vesuva vesuvaCard = new com.github.laxika.magicalvibes.cards.v.Vesuva();
        harness.setHand(player1, List.of(vesuvaCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();
        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == vesuvaCard).findFirst().orElseThrow();

        assertThat(gd.perpetualAbilities(vesuvaCard.getId())).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, vesuva)).hasSize(2);
    }

    @Test
    @DisplayName("Declining leaves Gitrog untapped and does not seek")
    void decliningLeavesGitrogUntappedAndDoesNotSeek() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gitrog.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers during an opponent's combat and seeks for Gitrog's controller")
    void triggersDuringOpponentsCombatAndSeeksForGitrogsController() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new FieldOfRuin()));

        advanceToCombatAndResolve(player2);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gitrog.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Field of Ruin");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tapping Gitrog before resolution prevents the sacrifice choice")
    void tappingGitrogBeforeResolutionPreventsTheSacrificeChoice() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        gitrog.tap();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifice still taps Gitrog when no land can be sought")
    void sacrificeStillTapsGitrogWhenNoLandCanBeSought() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gitrog.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing taps Gitrog and seeks before any further priority window")
    void sacrificingTapsGitrogAndSeeksBeforeAnyFurtherPriorityWindow() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombatAndResolve(player1);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThat(gitrog.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Land retains sacrifice draw ability after Gitrog leaves")
    void landRetainsSacrificeDrawAbilityAfterGitrogLeaves() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(gitrog);
        harness.setGraveyard(player1, List.of(gitrog.getCard()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent chooses which nontoken creature to sacrifice")
    void opponentChoosesWhichNontokenCreatureToSacrifice() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombatAndResolve(player1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, secondBear.getId());
        resolveAllTriggers();

        assertThat(gitrog.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstBear).doesNotContain(secondBear);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondBear.getCard());
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("No opponent creature leaves Gitrog untapped without seeking")
    void noOpponentCreatureLeavesGitrogUntappedWithoutSeeking() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gitrog.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrificed land retains its ability when it enters again without Gitrog")
    void sacrificedLandRetainsItsAbilityWhenItEntersAgainWithoutGitrog() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        Forest forestCard = new Forest();
        Permanent forest = harness.enterBattlefieldAndReturn(player1, forestCard);
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Forest");

        gd.playerBattlefields.get(player1.getId()).remove(gitrog);
        gd.playerGraveyards.get(player1.getId()).remove(forestCard);
        Permanent returnedForest = harness.enterBattlefieldAndReturn(player1, forestCard);
        resolveAllTriggers();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returnedForest), null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land that left the battlefield before the landfall ability resolves gains nothing (CR 400.7)")
    void landThatLeftGainsNothing() {
        harness.addToBattlefield(player1, new GitrogHorrorOfZhava());
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.perpetualAbilities(forest.getCard().getId())).isEmpty();
    }
}
