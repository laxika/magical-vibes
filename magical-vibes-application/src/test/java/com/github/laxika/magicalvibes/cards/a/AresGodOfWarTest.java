package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AresGodOfWar.class, GrizzlyBears.class})
class AresGodOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Ares must attack each combat when able")
    void mustAttackWhenAble() {
        addReady(new AresGodOfWar(), player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Returns an attacking creature you control to its owner's hand")
    void returnsAttackingAllyToHand() {
        addReady(new AresGodOfWar(), player1);
        Permanent attacker = addReady(new GrizzlyBears(), player1);
        attacker.setAttacking(true);

        putIntoGraveyard(attacker);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(attacker.getCard());
    }

    @Test
    @DisplayName("Returns Ares to its owner's hand when Ares dies attacking")
    void returnsSelfToHandWhenAttacking() {
        Permanent ares = addReady(new AresGodOfWar(), player1);
        ares.setAttacking(true);

        putIntoGraveyard(ares);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ares.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ares.getCard());
    }

    @Test
    @DisplayName("Does not return a creature that dies without attacking")
    void doesNotReturnNonattackingCreature() {
        addReady(new AresGodOfWar(), player1);
        Permanent creature = addReady(new GrizzlyBears(), player1);

        putIntoGraveyard(creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getCard());
    }

    private Permanent addReady(Card card, Player player) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
