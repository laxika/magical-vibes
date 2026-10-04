package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MoltenMonstrosity;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.r.RonasVortex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldenArgosy.class, MoltenMonstrosity.class, RayOfCommand.class, RonasVortex.class})
class GoldenArgosyTest extends BaseCardTest {

    @Test
    void attacksExileCreaturesThatCrewedItAndReturnThemTappedAtNextEndStep() {
        Permanent argosy = harness.addToBattlefieldAndReturn(player1, new GoldenArgosy());
        argosy.setSummoningSick(false);
        Permanent crewer = addCreatureReady(player1, new MoltenMonstrosity());
        Permanent bystander = addCreatureReady(player1, new MoltenMonstrosity());

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.PermanentChoice crewChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (crewChoice != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crewer);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(argosy, bystander);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Molten Monstrosity"))
                .hasSize(2)
                .anyMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(argosy, bystander);
    }

    @Test
    void exilesAllCreaturesSelectedForOneCrewActivation() {
        Permanent argosy = addCreatureReady(player1, new GoldenArgosy());
        Permanent first = addCreatureReady(player1, new MoltenMonstrosity());
        Permanent second = addCreatureReady(player1, new MoltenMonstrosity());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(argosy).doesNotContain(first, second);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Molten Monstrosity")).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    void exilesCrewerEvenIfAnOpponentGainsControlBeforeAttackTriggerResolves() {
        addCreatureReady(player1, new GoldenArgosy());
        Permanent crewer = addCreatureReady(player1, new MoltenMonstrosity());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, crewer.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(crewer);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(crewer);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Molten Monstrosity")).singleElement().matches(Permanent::isTapped);
        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
    }

    @Test
    void stolenCrewerReturnsTappedUnderItsOwnersControl() {
        addCreatureReady(player1, new GoldenArgosy());
        Permanent crewer = addCreatureReady(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, crewer.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Molten Monstrosity");
        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Molten Monstrosity")).singleElement().matches(Permanent::isTapped);
        harness.assertNotOnBattlefield(player1, "Molten Monstrosity");
    }

    @Test
    void creaturesReturnEvenIfArgosyLeavesBeforeEndStep() {
        Permanent argosy = addCreatureReady(player1, new GoldenArgosy());
        addCreatureReady(player1, new MoltenMonstrosity());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.setHand(player2, List.of(new RonasVortex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, argosy.getId());
        harness.assertInHand(player1, "Golden Argosy");
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Molten Monstrosity")).singleElement().matches(Permanent::isTapped);
    }

    @Test
    void crewerThatLeavesBeforeAttackTriggerResolvesIsNotReturned() {
        addCreatureReady(player1, new GoldenArgosy());
        Permanent crewer = addCreatureReady(player1, new MoltenMonstrosity());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        harness.setHand(player2, List.of(new RonasVortex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, crewer.getId());
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Molten Monstrosity");
        harness.assertNotOnBattlefield(player1, "Molten Monstrosity");
    }
}