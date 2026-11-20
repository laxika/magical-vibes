package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GearbaneOrangutan;
import com.github.laxika.magicalvibes.cards.r.RedHerring;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorneredCrook.class, RedHerring.class, GearbaneOrangutan.class})
class CorneredCrookTest extends BaseCardTest {

    private void castCrookToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CorneredCrook(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrificing an artifact deals 3 damage to a target player")
    void sacrificeArtifactDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new RedHerring());
        int lifeBefore = gd.getLife(player2.getId());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInGraveyard(player1, "Red Herring");
    }

    @Test
    @DisplayName("Sacrificing an artifact deals 3 damage to a target creature")
    void sacrificeArtifactDealsDamageToCreature() {
        harness.addToBattlefield(player1, new RedHerring());
        harness.addToBattlefield(player2, new GearbaneOrangutan());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Gearbane Orangutan"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Red Herring");
        harness.assertInGraveyard(player2, "Gearbane Orangutan");
    }

    @Test
    @DisplayName("Declining the sacrifice deals no damage and keeps the artifact")
    void decliningSacrificeDealsNoDamage() {
        harness.addToBattlefield(player1, new RedHerring());
        int lifeBefore = gd.getLife(player2.getId());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Red Herring");
    }

    @Test
    @DisplayName("Accepting without an artifact does not create a damage trigger")
    void noArtifactDealsNoDamage() {
        harness.addToBattlefield(player2, new RedHerring());
        int lifeBefore = gd.getLife(player2.getId());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player2, "Red Herring");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The sacrifice precedes target selection and damage waits for priority passes")
    void damageIsASeparateTriggerAndCanTargetController() {
        harness.addToBattlefield(player1, new RedHerring());
        int lifeBefore = gd.getLife(player1.getId());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Red Herring");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only a controlled artifact can be sacrificed")
    void rejectsOpponentsArtifactAndOwnNonartifact() {
        Permanent herring = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        Permanent otherHerring = harness.addToBattlefieldAndReturn(player1, new RedHerring());
        harness.addToBattlefield(player1, new GearbaneOrangutan());
        harness.addToBattlefield(player2, new RedHerring());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(herring.getId(), otherHerring.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(herring.getId()));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gearbane Orangutan"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(herring.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherHerring);
        harness.assertInGraveyard(player1, "Gearbane Orangutan");
        harness.assertOnBattlefield(player2, "Red Herring");
    }
}
