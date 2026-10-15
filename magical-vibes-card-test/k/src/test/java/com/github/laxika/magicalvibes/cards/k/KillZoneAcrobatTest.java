package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillZoneAcrobat.class, EnergyRefractor.class, Cloudshift.class})
class KillZoneAcrobatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Kill-Zone Acrobat flying")
    void sacrificingAnotherCreatureGrantsFlying() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent otherCreature = addCreatureReady(player1, new KillZoneAcrobat());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());

        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature.getCard());
    }

    @Test
    @DisplayName("Sacrificing another artifact gives Kill-Zone Acrobat flying")
    void sacrificingAnotherArtifactGrantsFlying() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not give Kill-Zone Acrobat flying")
    void decliningSacrificeDoesNothing() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent otherCreature = addCreatureReady(player1, new KillZoneAcrobat());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent otherCreature = addCreatureReady(player1, new KillZoneAcrobat());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());
        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Acrobat cannot sacrifice itself when there is no other eligible permanent")
    void noOtherPermanentDoesNotGrantFlying() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(acrobat);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice choices exclude the source and opposing creatures and artifacts")
    void sacrificeChoicesOnlyIncludeOtherControlledPermanents() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        Permanent opposingCreature = addCreatureReady(player2, new KillZoneAcrobat());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(ownArtifact.getId());

        harness.handlePermanentChosen(player1, ownArtifact.getId());

        assertThat(gqs.hasKeyword(gd, acrobat, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature, opposingArtifact);
    }

    @Test
    @DisplayName("A blinked Acrobat is eligible to sacrifice to its old attack trigger")
    void returnedAcrobatCanBeSacrificedToOldTrigger() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.castAndResolveInstant(player1, 0, acrobat.getId());
        Permanent returned = findPermanent(player1, "Kill-Zone Acrobat");
        assertThat(returned.getId()).isNotEqualTo(acrobat.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(returned.getId(), artifact.getId());

        harness.handlePermanentChosen(player1, returned.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact).doesNotContain(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned.getCard());
    }

    @Test
    @DisplayName("A blinked Acrobat does not gain flying from its old attack trigger")
    void returnedAcrobatDoesNotGainFlyingFromOldTrigger() {
        Permanent acrobat = addCreatureReady(player1, new KillZoneAcrobat());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.castAndResolveInstant(player1, 0, acrobat.getId());
        Permanent returned = findPermanent(player1, "Kill-Zone Acrobat");
        assertThat(returned.getId()).isNotEqualTo(acrobat.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
    }
}
