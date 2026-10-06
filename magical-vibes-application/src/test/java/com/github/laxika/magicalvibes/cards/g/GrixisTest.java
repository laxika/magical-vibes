package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Grixis.class, GoblinPiker.class, GrizzlyBears.class, Shock.class,
        FugitiveWizard.class, ScatheZombies.class})
class GrixisTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Grixis(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void grantsUnearthForTheGraveyardCardManaCost() {
        GoblinPiker target = new GoblinPiker();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Goblin Piker"));
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void grantsUnearthToBlueCreatures() {
        harness.setGraveyard(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    void grantsUnearthToBlackCreatures() {
        harness.setGraveyard(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scathe Zombies");
        harness.assertNotInGraveyard(player1, "Scathe Zombies");
    }

    @Test
    void unearthRequiresTheColoredManaInTheCardsCost() {
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
    }

    @Test
    void unearthCannotBeActivatedOutsideAMainPhase() {
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    @Test
    void leavingGrixisRemovesTheGrantedUnearthAbility() {
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.planechase.faceUp.clear();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unearthedCreatureIsExiledAtTheNextEndStepEvenAfterLeavingGrixis() {
        GoblinPiker target = new GoblinPiker();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        gd.planechase.faceUp.clear();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        harness.assertNotInGraveyard(player1, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void lethalDamageExilesAnUnearthedCreatureInsteadOfPuttingItInTheGraveyard() {
        GoblinPiker target = new GoblinPiker();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Goblin Piker"));

        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        harness.assertNotInGraveyard(player1, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void chaosDoesNotReturnACreatureThatLeftTheGraveyardBeforeResolution() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellGraveyardTargetTrigger(gd));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void doesNotGrantUnearthToGreenCreaturesOrNoncreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chaosReturnsATargetCreatureFromAnyGraveyardUnderItsControllersControl() {
        Card invalidTarget = new Shock();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(invalidTarget, target));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellGraveyardTargetTrigger(gd));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }
}
