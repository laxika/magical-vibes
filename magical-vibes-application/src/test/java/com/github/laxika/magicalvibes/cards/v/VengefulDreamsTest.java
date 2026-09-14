package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulDreams.class, AngelOfRetribution.class})
class VengefulDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X target attacking creatures after discarding X cards")
    void exilesExactlyXAttackingCreatures() {
        Permanent first = addCreatureReady(player1, new AngelOfRetribution());
        Permanent second = addCreatureReady(player1, new AngelOfRetribution());
        Permanent remaining = addCreatureReady(player1, new AngelOfRetribution());
        addCreatureReady(player2, new AngelOfRetribution());
        declareAttackers(List.of(0, 1, 2));

        prepareDeclareBlockers();
        harness.setHand(player2, List.of(new VengefulDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstantForXWithDiscards(player2, 0, 2,
                List.of(first.getId(), second.getId()), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getCard().getId(), second.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(remaining.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires exactly X attacking creature targets")
    void requiresExactlyXAttackingCreatureTargets() {
        Permanent attacker = addCreatureReady(player1, new AngelOfRetribution());
        addCreatureReady(player2, new AngelOfRetribution());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        harness.setHand(player2, List.of(
                new VengefulDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player2, 0, 2,
                List.of(attacker.getId()), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles the remaining legal target when another target leaves before resolution")
    void exilesRemainingLegalTargetWhenAnotherLeavesBeforeResolution() {
        Permanent surviving = addCreatureReady(player1, new AngelOfRetribution());
        Permanent removed = addCreatureReady(player1, new AngelOfRetribution());
        addCreatureReady(player2, new AngelOfRetribution());
        declareAttackers(List.of(0, 1));

        prepareDeclareBlockers();
        harness.setHand(player2, List.of(
                new VengefulDreams(), new AngelOfRetribution(), new AngelOfRetribution()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstantForXWithDiscards(player2, 0, 2,
                List.of(surviving.getId(), removed.getId()), List.of(1, 2));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removed));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(surviving.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast for X without enough cards to discard")
    void cannotCastWithoutEnoughCardsToDiscard() {
        Permanent attacker = addCreatureReady(player1, new AngelOfRetribution());
        addCreatureReady(player2, new AngelOfRetribution());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        harness.setHand(player2, List.of(new VengefulDreams()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player2, 0, 1,
                List.of(attacker.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Vengeful Dreams");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("X=0 requires no discard and exiles nothing")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new VengefulDreams()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantForXWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Vengeful Dreams");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new AngelOfRetribution());
        Permanent bystander = addCreatureReady(player1, new AngelOfRetribution());
        addCreatureReady(player2, new AngelOfRetribution());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        harness.setHand(player2, List.of(new VengefulDreams(), new AngelOfRetribution()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player2, 0, 1,
                List.of(bystander.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(attacker.getId(), bystander.getId());
    }
}
