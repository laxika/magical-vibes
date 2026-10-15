package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.p.PawnOfUlamog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlamogsCrusher.class, GrizzlyBears.class, NestInvader.class, PawnOfUlamog.class})
class UlamogsCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the defending player sacrifice two permanents")
    void annihilatorTwo() {
        Permanent crusher = addCreatureReady(player1, new UlamogsCrusher());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(crusher)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Must attack each combat if able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new UlamogsCrusher());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void defendingPlayerChoosesExactlyTwoPermanents() {
        addCreatureReady(player1, new UlamogsCrusher());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new NestInvader());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void sacrificesOnlyPermanentWhenDefenderHasFewerThanTwo() {
        addCreatureReady(player1, new UlamogsCrusher());
        harness.addToBattlefield(player2, new NestInvader());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void annihilatorResolvesWithNoDefendingPermanents() {
        addCreatureReady(player1, new UlamogsCrusher());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedCrusherIsNotRequiredToAttack() {
        Permanent crusher = addCreatureReady(player1, new UlamogsCrusher());
        crusher.tap();

        declareAttackers(List.of());

        assertThat(crusher.isAttacking()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCrusherIsNotRequiredToAttack() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new UlamogsCrusher());

        declareAttackers(List.of());

        assertThat(crusher.isAttacking()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void annihilatorSacrificesBothPawnsSimultaneously() {
        addCreatureReady(player1, new UlamogsCrusher());
        harness.addToBattlefield(player2, new PawnOfUlamog());
        harness.addToBattlefield(player2, new PawnOfUlamog());

        declareAttackers(List.of(0));
        for (int i = 0; i < 4; i++) {
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player2, true);
        }
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Pawn of Ulamog")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }
}
