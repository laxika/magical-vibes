package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReturnToDust;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalArchmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultSuit.class, GrizzlyBears.class, ReturnToDust.class, WitchbaneOrb.class, TeferiTemporalArchmage.class})
class AssaultSuitTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostHasteAndSacrificeProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSuitTo(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, creature)).isTrue();
    }

    @Test
    void opponentMayControlAndUntapEquippedCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachSuitTo(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player2.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));
        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player1.getId());
        assertThat(gqs.cantBeSacrificed(gd, creature)).isTrue();
    }


    @Test
    void decliningControlChangeDoesNotUntapCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachSuitTo(creature);
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player1.getId());
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerDuringControllersUpkeep() {
        attachSuitTo(addCreatureReady(player1, new GrizzlyBears()));
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentWithHexproofCanStillReceiveCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachSuitTo(creature);
        harness.addToBattlefield(player2, new WitchbaneOrb());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player2.getId());
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void controlChangeUsesLastKnownAttachmentAfterSuitIsExiled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent suit = attachSuitTo(creature);
        advanceToUpkeep(player2);
        harness.setHand(player1, List.of(new ReturnToDust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, List.of(suit.getId()));
        assertThat(gqs.findPermanentById(gd, suit.getId())).isNull();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player2.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.cantBeSacrificed(gd, creature)).isFalse();
    }

    @Test
    void controlChangeUsesCreatureEquippedAtResolution() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        original.tap();
        replacement.tap();
        Permanent suit = attachSuitTo(original);
        advanceToUpkeep(player2);
        suit.setAttachedTo(replacement.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.findPermanentController(gd, original.getId())).isEqualTo(player1.getId());
        assertThat(original.isTapped()).isTrue();
        assertThat(gqs.findPermanentController(gd, replacement.getId())).isEqualTo(player2.getId());
        assertThat(replacement.isTapped()).isFalse();
    }

    @Test
    void equipAbilityAttachesSuitForThreeMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }


    @Test
    void equippedCreatureCannotAttackSuitControllerOrTheirPlaneswalker() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent suit = attachSuitTo(creature);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new TeferiTemporalArchmage());
        assertThat(als.canAttackDefender(gd, creature, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, creature, planeswalker.getId())).isFalse();
        suit.setAttachedTo(null);
        assertThat(als.canAttackDefender(gd, creature, player1.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, creature, planeswalker.getId())).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, creature)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void unattachedSuitDoesNotGiveAwayOrUntapAnyCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        harness.addToBattlefield(player1, new AssaultSuit());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player1.getId());
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent attachSuitTo(Permanent creature) {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(creature.getId());
        return suit;
    }
}
