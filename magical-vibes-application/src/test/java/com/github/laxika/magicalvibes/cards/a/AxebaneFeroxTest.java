package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AxebaneFerox.class, GiantGrowth.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class})
class AxebaneFeroxTest extends BaseCardTest {

    @Test
    void wardCountersSpellWhenControllerCannotCollectEvidence() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, ferox.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
    }

    @Test
    void decliningToCollectEvidenceCountersSpell() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(first, second));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, ferox.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, shock);
    }

    @Test
    void collectingEvidenceLetsTargetedSpellResolveAndExilesChosenCards() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        GiantGrowth spell = new GiantGrowth();
        harness.setGraveyard(player2, List.of(first, second));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, ferox.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gqs.getEffectivePower(gd, ferox)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ferox)).isEqualTo(7);
    }

    @Test
    void controllersOwnSpellDoesNotTriggerWard() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, ferox.getId());

        assertThat(gqs.getEffectivePower(gd, ferox)).isEqualTo(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    void evidenceFromFeroxControllersGraveyardCannotPayOpponentsWard() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Card evidence = new AxebaneFerox();
        harness.setGraveyard(player1, List.of(evidence));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, ferox.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void collectingMoreThanFourManaValueLeavesUnselectedCardsInGraveyard() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Card first = new AxebaneFerox();
        Card second = new Shock();
        Card unselected = new Shock();
        harness.setGraveyard(player2, List.of(first, second, unselected));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Shock spell = new Shock();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, ferox.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected, spell);
        assertThat(ferox.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentsActivatedAbilityWithoutRemovingItsSource() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, ferox.getId());
        harness.passBothPriorities();

        assertThat(ferox.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void collectingEvidenceAllowsOpponentsActivatedAbilityToResolve() {
        Permanent ferox = harness.addToBattlefieldAndReturn(player1, new AxebaneFerox());
        addCreatureReady(player2, new ProdigalPyromancer());
        Card evidence = new AxebaneFerox();
        harness.setGraveyard(player2, List.of(evidence));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, ferox.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultipleCardsChosen(player2, List.of(evidence.getId()));
        harness.passBothPriorities();

        assertThat(ferox.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(evidence);
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void canAttackOnTheTurnItEntersTheBattlefield() {
        harness.setHand(player1, List.of(new AxebaneFerox()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void deathtouchDestroysABlockerWithMoreToughnessThanDamageDealt() {
        harness.addToBattlefield(player1, new AxebaneFerox());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, blocker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Axebane Ferox");
        harness.assertLife(player2, 20);
    }
}
