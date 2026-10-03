package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CylianSunsinger;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrackwaterElemental.class, CylianSunsinger.class, Unsummon.class})
@DisplayName("Brackwater Elemental")
class BrackwaterElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking schedules a sacrifice that fires at the next end step (survives combat)")
    void attackingSacrificesAtNextEndStep() {
        addCreatureReady(player1, new BrackwaterElemental());

        declareAttackers(List.of(0));

        // Resolve the attack trigger in the postcombat main phase: the sacrifice is delayed to the
        // next end step, so the elemental is still around after combat.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Brackwater Elemental");

        // Reaching the end step sacrifices it.
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Brackwater Elemental");
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
        harness.assertInGraveyard(player1, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Blocking schedules a sacrifice that fires at the next end step")
    void blockingSacrificesAtNextEndStep() {
        addCreatureReady(player2, new BrackwaterElemental());

        Permanent attacker = addCreatureReady(player1, new CylianSunsinger());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Brackwater Elemental");

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player2, "Brackwater Elemental");
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Brackwater Elemental");
        harness.assertInGraveyard(player2, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Unearth returns Brackwater Elemental to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Brackwater Elemental");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Unearthed Brackwater Elemental is exiled at the next end step")
    void unearthExiledAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Brackwater Elemental");
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brackwater Elemental"));
    }

    @Test
    @DisplayName("An elemental that neither attacks nor blocks survives the end step")
    void idleElementalSurvivesEndStep() {
        harness.addToBattlefield(player1, new BrackwaterElemental());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Brackwater Elemental");
        harness.assertNotInGraveyard(player1, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Unearth returns only the activated copy from the graveyard")
    void unearthReturnsOnlyActivatedCopy() {
        BrackwaterElemental first = new BrackwaterElemental();
        BrackwaterElemental second = new BrackwaterElemental();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(findPermanent(player1, "Brackwater Elemental").getCard().getId())
                .isEqualTo(second.getId());
    }

    @Test
    @DisplayName("Unearth cannot be activated during combat")
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Brackwater Elemental");
        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Unearth cannot be activated during an opponent's main phase")
    void unearthCannotBeActivatedOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Unearth requires blue mana in addition to its generic cost")
    void unearthCannotBePaidWithOnlyColorlessMana() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Brackwater Elemental");
        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
    }

    @Test
    @DisplayName("Unearth cannot be activated while another ability is on the stack")
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental(), new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Bouncing an unearthed elemental exiles it instead of returning it to hand")
    void unearthedElementalIsExiledInsteadOfBounced() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent elemental = findPermanent(player1, "Brackwater Elemental");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
        harness.assertNotInHand(player1, "Brackwater Elemental");
        harness.assertNotInGraveyard(player1, "Brackwater Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(elemental.getCard().getId()));
    }

    @Test
    @DisplayName("An unearthed elemental can attack immediately and is exiled after attacking")
    void unearthedElementalCanAttackAndIsExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new BrackwaterElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Brackwater Elemental");
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Brackwater Elemental");
        harness.assertNotInGraveyard(player1, "Brackwater Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brackwater Elemental"));
    }

    @Test
    @DisplayName("A delayed sacrifice does not follow an elemental bounced and recast before the end step")
    void delayedSacrificeDoesNotAffectRecastElemental() {
        addCreatureReady(player1, new BrackwaterElemental());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        resolveAllTriggers();
        Permanent original = findPermanent(player1, "Brackwater Elemental");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.assertInHand(player1, "Brackwater Elemental");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Brackwater Elemental").getId())
                .isNotEqualTo(original.getId());

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Brackwater Elemental");
        harness.assertNotInGraveyard(player1, "Brackwater Elemental");
    }
}
