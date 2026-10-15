package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MatterReshaper;
import com.github.laxika.magicalvibes.cards.s.Shapesharer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrainstealerDragon.class, GrizzlyBears.class, CommandTower.class, Shapesharer.class, MatterReshaper.class})
class BrainstealerDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each opponent's top card with persistent any-color play permission")
    void exilesEachOpponentsTopCardWithPersistentPermission() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));
        harness.addToBattlefield(player1, new BrainstealerDragon());

        resolveEndStepTrigger();

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    @Test
    @DisplayName("A nonland permanent cast from exile makes its owner lose its mana value")
    void castOpponentOwnedPermanentMakesOwnerLoseManaValueLife() {
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));
        harness.addToBattlefield(player1, new BrainstealerDragon());

        resolveEndStepTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty opposing library is ignored without drawing a card")
    void emptyOpponentLibraryIsIgnored() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new BrainstealerDragon());

        resolveEndStepTrigger();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent's end step does not exile cards")
    void doesNotTriggerAtOpponentsEndStep() {
        Card topCard = new BrainstealerDragon();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new BrainstealerDragon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An opponent-owned land can be played without causing life loss")
    void exiledLandCanBePlayedWithoutLifeLoss() {
        Card land = new CommandTower();
        land.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(land));
        harness.addToBattlefield(player1, new BrainstealerDragon());
        resolveEndStepTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Command Tower");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A permanent owned by the Dragon's controller causes no life loss")
    void ownPermanentDoesNotTriggerLifeLoss() {
        harness.addToBattlefield(player1, new BrainstealerDragon());
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player1.getId());

        harness.enterBattlefieldAndReturn(player1, creature);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A permanent entering under the opponent's control causes no life loss")
    void opponentControlledPermanentDoesNotTriggerLifeLoss() {
        harness.addToBattlefield(player1, new BrainstealerDragon());
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());

        harness.enterBattlefieldAndReturn(player2, creature);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life loss also applies to opponent-owned permanents that were not exiled")
    void permanentEnteringWithoutBeingCastFromExileTriggersLifeLoss() {
        harness.addToBattlefield(player1, new BrainstealerDragon());
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());

        harness.enterBattlefieldAndReturn(player1, creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Exiled cards remain playable with other colors after the Dragon leaves")
    void permissionSurvivesDragonLeavingBattlefield() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature));
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BrainstealerDragon());
        resolveEndStepTrigger();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dragon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Persistent exile permission does not bypass creature casting timing")
    void creatureCannotBeCastDuringEndStep() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature));
        harness.addToBattlefield(player1, new BrainstealerDragon());
        resolveEndStepTrigger();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    @CardUsed({BrainstealerDragon.class, Shapesharer.class})
    @DisplayName("Life loss uses the entering permanent's mana value when the trigger resolves")
    void usesManaValueAfterCopyEffectResolves() {
        Card creature = new Shapesharer();
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature));
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new BrainstealerDragon());
        resolveEndStepTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        Permanent shapesharer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 1, 0,
                List.of(shapesharer.getId(), dragon.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
    }

    @Test
    @CardUsed({BrainstealerDragon.class, MatterReshaper.class})
    @DisplayName("Any-color spending permission does not replace required colorless mana")
    void coloredManaCannotPayRequiredColorlessSymbol() {
        Card creature = new MatterReshaper();
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature));
        harness.addToBattlefield(player1, new BrainstealerDragon());
        resolveEndStepTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
    }
}
