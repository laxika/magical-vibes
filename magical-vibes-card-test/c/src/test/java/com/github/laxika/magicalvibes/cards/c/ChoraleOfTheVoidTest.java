package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightLuminary;
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

@CardUsed({ChoraleOfTheVoid.class, Forest.class, GrizzlyBears.class, KnightLuminary.class})
class ChoraleOfTheVoidTest extends BaseCardTest {

    @Test
    void cannotEnchantOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentsCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChoraleOfTheVoid()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentsCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void returnsCreatureFromDefendingGraveyardTappedAndAttacking() {
        Permanent attacker = addAttachedChorale();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(creature, land));

        declareAttack(attacker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void sacrificesAtEndStepWithoutVoidEvent() {
        addAttachedChorale();

        goToEndStep();

        harness.assertNotOnBattlefield(player1, "Chorale of the Void");
    }

    @Test
    void survivesEndStepAfterNonlandPermanentLeftTheBattlefield() {
        addAttachedChorale();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        goToEndStep();

        assertThat(findPermanent(player1, "Chorale of the Void")).isNotNull();
    }

    @Test
    void sacrificesAtEndStepWhenOnlyALandLeftTheBattlefield() {
        addAttachedChorale();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        goToEndStep();

        harness.assertNotOnBattlefield(player1, "Chorale of the Void");
    }

    @Test
    void doesNotReturnCreaturesFromItsControllersGraveyard() {
        Permanent attacker = addAttachedChorale();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Forest()));

        declareAttack(attacker);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotReturnTargetThatLeftTheGraveyardInResponse() {
        Permanent attacker = addAttachedChorale();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        declareAttack(attacker);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void survivesWhenNonlandPermanentLeavesInResponseToEndStepTrigger() {
        addAttachedChorale();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        harness.assertOnBattlefield(player1, "Chorale of the Void");
    }

    @Test
    void survivesEndStepAfterSpellWasWarped() {
        addAttachedChorale();
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.nonlandPermanentLeftBattlefieldThisTurn).isFalse();

        goToEndStep();

        harness.assertOnBattlefield(player1, "Chorale of the Void");
    }

    @Test
    void doesNotSacrificeDuringOpponentsEndStep() {
        addAttachedChorale();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Chorale of the Void");
    }

    @Test
    void attachesToItsControllersCreatureWhenCast() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChoraleOfTheVoid()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Chorale of the Void").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    private Permanent addAttachedChorale() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);

        Permanent chorale = harness.addToBattlefieldAndReturn(player1, new ChoraleOfTheVoid());
        chorale.setAttachedTo(creature.getId());
        return creature;
    }

    private void declareAttack(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
    }

    private void goToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
