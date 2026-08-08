package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EsperStormblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AweForTheGuildsTest extends BaseCardTest {

    @Test
    @DisplayName("Monocolored creatures can't block this turn")
    void monocoloredCantBlock() {
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        castAweForTheGuilds();

        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Multicolored and colorless creatures are unaffected")
    void othersUnaffected() {
        Permanent stormblade = addReadyCreature(player2, new EsperStormblade());
        Permanent thopter = addReadyCreature(player2, new Ornithopter());

        castAweForTheGuilds();

        assertThat(stormblade.isCantBlockThisTurn()).isFalse();
        assertThat(thopter.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Applies to both players' monocolored creatures")
    void affectsAllPlayers() {
        Permanent ownBears = addReadyCreature(player1, new GrizzlyBears());
        Permanent oppBears = addReadyCreature(player2, new GrizzlyBears());

        castAweForTheGuilds();

        assertThat(ownBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppBears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A restricted monocolored creature can't be declared as a blocker")
    void restrictedCreatureCantBeDeclaredBlocker() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        castAweForTheGuilds();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A multicolored creature can still be declared as a blocker")
    void multicoloredCanStillBlock() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent stormblade = addReadyCreature(player2, new EsperStormblade());

        castAweForTheGuilds();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(stormblade.isBlocking()).isTrue();
    }

    private void castAweForTheGuilds() {
        harness.setHand(player1, List.of(new AweForTheGuilds()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
