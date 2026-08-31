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

import java.util.List;

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

    private Permanent castDrill() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new TheFireNationDrill()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "The Fire Nation Drill");
    }
}
