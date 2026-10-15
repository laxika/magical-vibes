package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.ScorchSpitter;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
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

@CardUsed({ThunderkinAwakener.class, EarthElemental.class, LlanowarElves.class,
        SparkElemental.class, ScorchSpitter.class, Murder.class})
class ThunderkinAwakenerTest extends BaseCardTest {

    private Permanent addReadyAwakener() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new ThunderkinAwakener());
        awakener.setSummoningSick(false);
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
        Card valid = new ScorchSpitter();
        harness.setGraveyard(player1, List.of(valid));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Scorch Spitter");
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Scorch Spitter");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Scorch Spitter");
        harness.assertInGraveyard(player1, "Scorch Spitter");
    }

    @Test
    void returnsElementalUsingLastKnownToughnessAfterAwakenerIsDestroyed() {
        Permanent awakener = addReadyAwakener();
        Card target = new ScorchSpitter();
        harness.setGraveyard(player1, List.of(target));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, awakener.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Thunderkin Awakener");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scorch Spitter");
        harness.assertNotInGraveyard(player1, "Scorch Spitter");
    }

    @Test
    void targetBecomesIllegalWhenAwakenersToughnessDecreases() {
        Permanent awakener = addReadyAwakener();
        Card target = new ScorchSpitter();
        harness.setGraveyard(player1, List.of(target));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        awakener.setToughnessModifier(-1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scorch Spitter");
        harness.assertInGraveyard(player1, "Scorch Spitter");
    }

    @Test
    void equalToughnessElementalIsNotALegalTarget() {
        addReadyAwakener();
        harness.setGraveyard(player1, List.of(new ThunderkinAwakener()));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void canAttackWhileSummoningSickAndCannotTargetOpponentsGraveyard() {
        Permanent awakener = harness.addToBattlefieldAndReturn(player1, new ThunderkinAwakener());
        awakener.setSummoningSick(true);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new ScorchSpitter()));

        declareAttack();

        assertThat(awakener.isAttacking()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Scorch Spitter");
    }

    @Test
    void usesModifiedToughnessAndDoesNotTriggerForReturnedAttacker() {
        Permanent awakener = addReadyAwakener();
        awakener.setToughnessModifier(1);
        Card target = new ThunderkinAwakener();
        harness.setGraveyard(player1, List.of(target));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(target.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
