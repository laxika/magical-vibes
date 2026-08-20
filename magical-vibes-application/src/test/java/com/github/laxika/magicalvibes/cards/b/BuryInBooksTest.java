package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuryInBooksTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature second from the top of its owner's library")
    void putsTargetCreatureSecondFromTop() {
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        Card topCard = new Island();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(topCard, new Island(), new Island()));

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0)).isSameAs(topCard);
        assertThat(library.get(1).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Costs {2}{U} when targeting an attacking creature")
    void costsReducedAmountWhenTargetingAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires the full cost when targeting a non-attacking creature")
    void requiresFullCostWhenTargetingNonAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can target a non-attacking creature when paying the full cost")
    void targetsNonAttackingCreatureAtFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player controller,
                                  com.github.laxika.magicalvibes.model.Player defender,
                                  Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(permanent);
        return permanent;
    }
}
