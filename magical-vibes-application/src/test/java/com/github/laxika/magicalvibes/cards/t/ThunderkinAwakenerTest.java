package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderkinAwakenerTest extends BaseCardTest {

    private Permanent addReadyAwakener() {
        Permanent awakener = new Permanent(new ThunderkinAwakener());
        awakener.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(awakener);
        return awakener;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    void onlyElementalsWithLowerToughnessCanBeChosen() {
        addReadyAwakener();
        Card valid = new SparkElemental();
        Card wrongSubtype = new LlanowarElves();
        Card tooTough = new EarthElemental();
        harness.setGraveyard(player1, List.of(valid, wrongSubtype, tooTough));

        declareAttack();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());
    }

    @Test
    void returnsChosenElementalTappedAndAttackingThenSacrificesIt() {
        addReadyAwakener();
        Card valid = new SparkElemental();
        harness.setGraveyard(player1, List.of(valid));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Spark Elemental");
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();

        assertThat(findPermanent(player1, "Spark Elemental")).isNotNull();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spark Elemental");
        harness.assertInGraveyard(player1, "Spark Elemental");
    }

    @Test
    void doesNotTriggerWhenNoGraveyardCardMatches() {
        addReadyAwakener();
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new EarthElemental()));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
