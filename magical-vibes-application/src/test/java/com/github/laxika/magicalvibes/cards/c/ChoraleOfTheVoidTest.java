package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChoraleOfTheVoid.class, Forest.class, GrizzlyBears.class})
class ChoraleOfTheVoidTest extends BaseCardTest {

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
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
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

    private Permanent addAttachedChorale() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent chorale = new Permanent(new ChoraleOfTheVoid());
        chorale.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(chorale);
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
