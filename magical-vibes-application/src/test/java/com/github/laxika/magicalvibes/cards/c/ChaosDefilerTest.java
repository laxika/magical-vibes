package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.a.ArcoFlagellant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosDefiler.class, GrizzlyBears.class, Murder.class, ArcaneSignet.class, ArcoFlagellant.class, Forest.class})
class ChaosDefilerTest extends BaseCardTest {

    @Test
    void entersAndDestroysTheChosenNonlandPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castChaosDefiler();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(second)
                .doesNotContain(first);
    }

    @Test
    void deathTriggerRepeatsTheEffect() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castChaosDefiler();
        harness.handlePermanentChosen(player1, first.getId());
        Permanent defiler = findPermanent(player1, "Chaos Defiler");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, defiler.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(defiler.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second);
    }

    @Test
    void destroysAnOpponentsOnlyNonlandPermanentWithoutAChoice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());

        castChaosDefiler();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        harness.assertInGraveyard(player2, "Arcane Signet");
        harness.assertOnBattlefield(player1, "Chaos Defiler");
    }

    @Test
    void doesNothingWhenOpponentControlsOnlyLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());

        castChaosDefiler();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        harness.assertOnBattlefield(player1, "Chaos Defiler");
    }

    @Test
    void choiceExcludesLandsAndTheControllersPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());

        castChaosDefiler();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, land).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    @Test
    void canChooseAnIndestructiblePermanentWithoutDestroyingAnotherInstead() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new ArcoFlagellant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castChaosDefiler();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(protectedCreature.getId(), artifact.getId());
        harness.handlePermanentChosen(player1, protectedCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(protectedCreature, artifact);
    }

    private void castChaosDefiler() {
        harness.castFromHand(player1, new ChaosDefiler(), "{3}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
