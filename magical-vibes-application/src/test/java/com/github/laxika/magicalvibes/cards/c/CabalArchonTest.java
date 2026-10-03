package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfGrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalArchon.class, DiscipleOfGrace.class, AphettoAlchemist.class, ArtificialEvolution.class, Bitterblossom.class})
@DisplayName("Cabal Archon")
class CabalArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing itself drains a target player for 2 life")
    void sacrificingItselfDrainsTargetPlayer() {
        addCreatureReady(player1, new CabalArchon());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Cabal Archon");
    }

    @Test
    @DisplayName("Can sacrifice another Cleric")
    void canSacrificeAnotherCleric() {
        addCreatureReady(player1, new CabalArchon());
        Permanent otherCleric = addCreatureReady(player1, new DiscipleOfGrace());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ActivatedAbilityCostChoice.class);
        harness.handlePermanentChosen(player1, otherCleric.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Cabal Archon");
        harness.assertInGraveyard(player1, "Disciple of Grace");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        addCreatureReady(player1, new CabalArchon());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Cabal Archon");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Cleric creature")
    void cannotSacrificeNonCleric() {
        addCreatureReady(player1, new CabalArchon());
        Permanent nonCleric = addCreatureReady(player1, new AphettoAlchemist());
        prepareAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(nonCleric).isIn(gd.playerBattlefields.get(player1.getId()));
        harness.assertInGraveyard(player1, "Cabal Archon");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent archon = addCreatureReady(player1, new CabalArchon());
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Archon can activate")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent archon = harness.addToBattlefieldAndReturn(player1, new CabalArchon());
        archon.setSummoningSick(true);
        archon.setTapped(true);
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Cabal Archon");
    }

    @Test
    @DisplayName("Sacrifice is paid before the life changes resolve")
    void sacrificeIsPaidBeforeResolution() {
        addCreatureReady(player1, new CabalArchon());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Cabal Archon");
        harness.assertNotOnBattlefield(player1, "Cabal Archon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new CabalArchon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cabal Archon");
        harness.assertNotInGraveyard(player1, "Cabal Archon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Cleric cannot be sacrificed")
    void cannotSacrificeOpponentsCleric() {
        addCreatureReady(player1, new CabalArchon());
        addCreatureReady(player2, new DiscipleOfGrace());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cabal Archon");
        harness.assertOnBattlefield(player2, "Disciple of Grace");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
    @Test
    @DisplayName("Can sacrifice a noncreature Kindred Cleric permanent")
    void canSacrificeNoncreatureCleric() {
        addCreatureReady(player1, new CabalArchon());
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, blossom.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "CLERIC");
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, blossom.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cabal Archon");
        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
    private void prepareAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
