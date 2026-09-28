package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraNalaar.class, CollectiveRestraint.class, Forest.class, Island.class,
        Mountain.class, Plains.class, RazorfootGriffin.class, Swamp.class})
class CollectiveRestraintTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent pays one generic mana per distinct basic land type")
    void opponentPaysDomainTax() {
        harness.addToBattlefield(player1, new CollectiveRestraint());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        addCreatureReady(player2, new RazorfootGriffin());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking without enough mana for the Domain tax is illegal")
    void opponentCannotAttackWithoutPayingDomainTax() {
        harness.addToBattlefield(player1, new CollectiveRestraint());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2, new RazorfootGriffin());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Only the defending player's basic land types count")
    void onlyDefendingPlayersDomainCounts() {
        harness.addToBattlefield(player1, new CollectiveRestraint());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        addCreatureReady(player2, new RazorfootGriffin());

        declareAttackers(player2, List.of(5));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attacking a defending planeswalker is not taxed")
    void attackingPlaneswalkerIsNotTaxed() {
        harness.addToBattlefield(player1, new CollectiveRestraint());
        harness.addToBattlefield(player1, new Plains());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player2, new RazorfootGriffin());

        declareAttackersAtTargets(player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveCombat(player2);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent pays the Domain tax for each attacking creature")
    void opponentPaysDomainTaxForEachAttacker() {
        harness.addToBattlefield(player1, new CollectiveRestraint());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new RazorfootGriffin());
        addCreatureReady(player2, new RazorfootGriffin());
        harness.addMana(player2, ManaColor.COLORLESS, 10);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private void declareAttackersAtTargets(Player player, List<Integer> attackerIndices,
                                           Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

}
