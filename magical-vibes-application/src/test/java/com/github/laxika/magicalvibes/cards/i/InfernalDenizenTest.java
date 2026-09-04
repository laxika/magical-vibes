package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalDenizen.class, GrizzlyBears.class, Swamp.class, Unsummon.class, RayOfCommand.class})
class InfernalDenizenTest extends BaseCardTest {

    private Permanent denizen(Player owner) {
        return findPermanent(owner, "Infernal Denizen");
    }

    @Test
    @DisplayName("With two Swamps, both are sacrificed and Denizen stays untapped")
    void sacrificesTwoSwampsNoPenalty() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → auto-sacrifice both

        harness.assertNotOnBattlefield(player1, "Swamp");
        assertThat(denizen(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("With more than two Swamps, exactly two are sacrificed")
    void sacrificesExactlyTwoOfMoreThanTwoSwamps() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        Permanent firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent secondSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent thirdSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstSwamp.getId(), secondSwamp.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(thirdSwamp)
                .doesNotContain(firstSwamp, secondSwamp);
        assertThat(denizen(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("With fewer than two Swamps, Denizen taps and opponent may steal a creature")
    void cannotSacrificeTapsAndOffersOpponentSteal() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve → penalty

        assertThat(denizen(player1).isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Swamp"); // the one Swamp is not sacrificed

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Opponent may decline the steal after Denizen taps")
    void opponentMayDeclineSteal() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(denizen(player1).isTapped()).isTrue();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Opponent may choose the Denizen itself for the upkeep penalty")
    void opponentMayChooseDenizenItself() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new InfernalDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(denizen.getId(), bears.getId());

        harness.handlePermanentChosen(player2, denizen.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(denizen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(denizen.getId()));
    }

    @Test
    @DisplayName("{T}: gains control of target creature while Denizen remains on the battlefield")
    void tapAbilityStealsCreature() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        Permanent denizen = denizen(player1);
        denizen.setSummoningSick(false);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int denizenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(denizen);
        harness.activateAbility(player1, denizenIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(denizen.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Stolen creature returns when Denizen leaves the battlefield")
    void controlEndsWhenDenizenLeaves() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        Permanent denizen = denizen(player1);
        denizen.setSummoningSick(false);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int denizenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(denizen);
        harness.activateAbility(player1, denizenIdx, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, denizen.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Creature stolen by the upkeep penalty returns when Denizen leaves")
    void upkeepControlEndsWhenDenizenLeaves() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID denizenId = harness.getPermanentId(player1, "Infernal Denizen");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, bearsId);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, denizenId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Activated control survives a temporary change of control of the Denizen")
    void activatedControlSurvivesTemporarySourceControlChange() {
        Permanent denizen = addCreatureReady(player1, new InfernalDenizen());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        int denizenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(denizen);
        harness.activateAbility(player1, denizenIndex, null, bears.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, denizen.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(denizen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(denizen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(denizen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new InfernalDenizen());
        harness.addToBattlefield(player1, new Swamp());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(denizen(player1).isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Swamp");
    }
}
