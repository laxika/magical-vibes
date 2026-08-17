package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationOfSaintTraftTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature creates a tapped and attacking Angel")
    void attackCreatesAngelToken() {
        Permanent bears = addCreatureReady(player1);
        attachInvocation(player1, bears);
        preventAutoPass(player2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The Angel token is exiled at end of combat")
    void angelTokenExiledAtEndOfCombat() {
        Permanent bears = addCreatureReady(player1);
        attachInvocation(player1, bears);
        preventAutoPass(player2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(
                        angel.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
    }

    @Test
    @DisplayName("The Angel token is created for the enchanted creature's controller")
    void tokenGoesToEnchantedController() {
        Permanent bears = addCreatureReady(player2);
        attachInvocation(player1, bears);
        preventAutoPass(player1);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void attachInvocation(Player auraController, Permanent creature) {
        Permanent aura = new Permanent(new InvocationOfSaintTraft());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
    }

    private void preventAutoPass(Player player) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
