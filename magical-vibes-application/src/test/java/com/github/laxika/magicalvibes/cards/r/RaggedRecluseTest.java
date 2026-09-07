package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OdiousWitch;
import com.github.laxika.magicalvibes.cards.t.ThirstForDiscovery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaggedRecluse.class, OdiousWitch.class, ThirstForDiscovery.class,
        Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class RaggedRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at the end of a turn in which its controller discarded a card")
    void transformsAfterControllerDiscards() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());
        setUpDiscardSpell();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(recluse.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at the end of a turn in which its controller did not discard")
    void doesNotTransformWithoutControllerDiscard() {
        Permanent recluse = harness.addToBattlefieldAndReturn(player1, new RaggedRecluse());

        advanceToEndStep();

        assertThat(recluse.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Odious Witch drains the defending player when it attacks")
    void backFaceDrainsDefendingPlayerWhenAttacking() {
        RaggedRecluse card = new RaggedRecluse();
        Permanent witch = addReadyCreature(player1, card);
        witch.setCard(card.getBackFaceCard());
        witch.setTransformed(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 16);
    }

    private void setUpDiscardSpell() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new ThirstForDiscovery(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    protected void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
