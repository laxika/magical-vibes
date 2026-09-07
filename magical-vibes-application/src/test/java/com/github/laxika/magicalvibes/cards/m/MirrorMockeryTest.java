package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorMockery.class, GrizzlyBears.class, GiantGrowth.class})
class MirrorMockeryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature offers to create a normal token copy")
    void attackingOffersTokenCopy() {
        Permanent creature = addReadyCreature(player1);
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent copy = findTokenCopy();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.isAttackedThisTurn()).isFalse();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(copy.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));
    }

    @Test
    @DisplayName("Declining the attack trigger does not create a token")
    void decliningDoesNotCreateToken() {
        Permanent creature = addReadyCreature(player1);
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The token copy is exiled at end of combat")
    void tokenCopyIsExiledAtEndOfCombat() {
        Permanent creature = addReadyCreature(player1);
        castMirrorMockery(creature);

        keepCombatOpen();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent copy = findTokenCopy();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    private void castMirrorMockery(Permanent creature) {
        harness.setHand(player1, List.of(new MirrorMockery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void keepCombatOpen() {
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
    }

    private Permanent findTokenCopy() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
