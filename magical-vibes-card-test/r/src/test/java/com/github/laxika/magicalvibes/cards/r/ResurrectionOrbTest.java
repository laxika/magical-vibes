package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.d.DoomedNecromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResurrectionOrb.class, GrizzlyBears.class, Deathmark.class,
        DoomedNecromancer.class, NantukoHusk.class})
class ResurrectionOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has lifelink")
    void equippedCreatureHasLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        orb.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void equippedCreatureCombatDamageGainsLife() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        orb.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(orb), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(orb.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(orb), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(orb.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {4} attaches Resurrection Orb to a creature you control")
    void equipsToCreatureYouControl() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(orb), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(orb.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature returns under its owner's control at the next end step")
    void returnsEquippedCreatureAtNextEndStep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        orb.setAttachedTo(creature.getId());

        destroyWithDeathmark(creature);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(orb.getAttachedTo()).isNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedEndStepTrigger.class)).isEmpty();
    }

    @Test
    void returnsToCreatureOwnerRatherThanEquipmentController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        orb.setAttachedTo(creature.getId());

        destroyWithDeathmark(creature);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(orb.getAttachedTo()).isNull();
    }

    @Test
    void doesNotReturnUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ResurrectionOrb());

        destroyWithDeathmark(creature);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void deathDuringEndStepWaitsUntilFollowingEndStep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        Permanent husk = addCreatureReady(player1, new NantukoHusk());
        orb.setAttachedTo(creature.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        sacrificeToHusk(husk, creature);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotReturnCardThatLeftAndReenteredGraveyardBeforeDeathTriggerResolved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        Permanent husk = addCreatureReady(player1, new NantukoHusk());
        Permanent necromancer = addCreatureReady(player1, new DoomedNecromancer());
        orb.setAttachedTo(creature.getId());

        sacrificeToHusk(husk, creature);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbilityWithGraveyardTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(necromancer), 0,
                List.of(creature.getCard().getId()));
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        sacrificeToHusk(husk, returned);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void sacrificeToHusk(Permanent husk, Permanent creature) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(husk),
                null, null);
        harness.handlePermanentChosen(player1, creature.getId());
    }

    private void destroyWithDeathmark(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player2, 0, creature.getId());
        resolveAllTriggers();
    }
}
