package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFireNationDrill.class, CrawWurm.class, GrizzlyBears.class, TrollAscetic.class,
        ZetalpaPrimalDawn.class})
class TheFireNationDrillTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may tap the Drill and then destroy a creature with power 4 or less")
    void etbTapsAndDestroysSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent drill = castDrill();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(drill.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB target selection excludes creatures with power greater than 4")
    void etbTargetSelectionUsesPowerRestriction() {
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        Permanent drill = castDrill();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(smallCreature.getId()).doesNotContain(largeCreature.getId());

        harness.handlePermanentChosen(player1, smallCreature.getId());
        harness.passBothPriorities();

        assertThat(drill.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("Declining the ETB tap leaves the Drill untapped and the creature alive")
    void decliningEtbTapDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent drill = castDrill();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(drill.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The activated ability removes both keywords from opposing permanents until end of turn")
    void removesOpponentKeywordsUntilEndOfTurn() {
        Permanent drill = harness.addToBattlefieldAndReturn(player1, new TheFireNationDrill());
        Permanent ownHexproof = harness.addToBattlefieldAndReturn(player1, new TrollAscetic());
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());
        Permanent opponentIndestructible = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(drill), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownHexproof, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentIndestructible, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Crew 2 animates the Drill without tapping it")
    void crewAnimatesDrill() {
        Permanent drill = harness.addToBattlefieldAndReturn(player1, new TheFireNationDrill());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(drill), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, drill)).isTrue();
        assertThat(drill.isTapped()).isFalse();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping the Drill with no legal destruction target is still allowed")
    void etbCanTapWithoutLegalTargets() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        Permanent drill = castDrill();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(drill.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(largeCreature);
    }

    @Test
    @DisplayName("An already tapped Drill cannot trigger the reflexive destruction ability")
    void alreadyTappedDrillDoesNotDestroy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new TheFireNationDrill(), "{2}{B}{B}");
        harness.passBothPriorities();
        Permanent drill = findPermanent(player1, "The Fire Nation Drill");
        drill.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Keyword removal does not affect permanents entering after resolution")
    void laterPermanentsKeepTheirKeywords() {
        Permanent drill = harness.addToBattlefieldAndReturn(player1, new TheFireNationDrill());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent hexproof = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());
        Permanent indestructible = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());

        assertThat(gqs.hasKeyword(gd, hexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, indestructible, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(drill.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Drill also loses keywords if an opponent controls it when its ability resolves")
    void sourceChangingControlIsIncludedInKeywordRemoval() {
        Permanent drill = harness.addToBattlefieldAndReturn(player1, new TheFireNationDrill());
        drill.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);
        drill.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(drill);
        gd.playerBattlefields.get(player2.getId()).add(drill);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drill, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, drill, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The reflexive ability can target your own hexproof creature but not an opponent's")
    void reflexiveTargetingRespectsHexproofAndAllowsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TrollAscetic());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());

        castDrill();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Troll Ascetic");
        harness.assertOnBattlefield(player2, "Troll Ascetic");
    }

    @Test
    @DisplayName("Power four is eligible and keyword removal can resolve before reflexive destruction")
    void removingIndestructibleInResponseAllowsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZetalpaPrimalDawn());

        Permanent drill = castDrill();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(drill), 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertOnBattlefield(player2, "Zetalpa, Primal Dawn");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Zetalpa, Primal Dawn");
    }

    @Test
    @DisplayName("A summoning-sick creature can crew, and the animation expires at cleanup")
    void summoningSickCreatureCanCrewUntilEndOfTurn() {
        Permanent drill = harness.addToBattlefieldAndReturn(player1, new TheFireNationDrill());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, drill)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, drill)).isFalse();
    }

    private Permanent castDrill() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new TheFireNationDrill(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "The Fire Nation Drill");
    }
}
