package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorinOakenshield.class, FountainOfYouth.class, GrizzlyBears.class,
        Insight.class, Shock.class, Disenchant.class, LiquimetalCoating.class})
class ThorinOakenshieldTest extends BaseCardTest {

    @Test
    void enduringStoryRequiresThreeQualifyingPermanents() {
        harness.enterBattlefieldAndReturn(player1, new ThorinOakenshield());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
    }

    @Test
    void enduringStoryGivesWardToOwnCreatureAndArtifact() {
        Permanent thorin = addStoriedThorin();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castOpponentShock(thorin);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, artifact.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Disenchant");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    void enduringStoryDoesNotGiveWardToOtherPermanentTypes() {
        addStoriedThorin();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Insight());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Insight");
    }

    @Test
    void changingAnExistingPermanentIntoAnArtifactGrantsEnduringStory() {
        harness.enterBattlefieldAndReturn(player1, new ThorinOakenshield());
        harness.enterBattlefieldAndReturn(player1, new LiquimetalCoating());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.playersWithEnduringStory).doesNotContain(player1.getId());

        harness.activateAbility(player1, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
    }

    @Test
    void wardCountersAnOpponentsTargetedActivatedAbility() {
        addStoriedThorin();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LiquimetalCoating());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(harness.getGameQueryService().isArtifact(gd, bears)).isFalse();
    }

    @Test
    void payingWardAllowsTheSpellToResolve() {
        Permanent thorin = addStoriedThorin();

        castOpponentShock(thorin);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thorin Oakenshield");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
    }

    @Test
    void withoutEnduringStoryThorinHasNoWard() {
        Permanent thorin = harness.enterBattlefieldAndReturn(player1, new ThorinOakenshield());

        castOpponentShock(thorin);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Thorin Oakenshield");
    }

    @Test
    void enduringStoryPersistsBelowThreeQualifyingPermanents() {
        Permanent thorin = addStoriedThorin();
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        harness.assertInGraveyard(player1, "Fountain of Youth");
        castOpponentShock(thorin);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertOnBattlefield(player1, "Thorin Oakenshield");
    }

    private Permanent addStoriedThorin() {
        Permanent thorin = harness.enterBattlefieldAndReturn(player1, new ThorinOakenshield());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        return thorin;
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
