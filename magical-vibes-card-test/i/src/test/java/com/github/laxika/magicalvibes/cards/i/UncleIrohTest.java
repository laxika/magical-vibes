package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.k.KnowledgeSeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UncleIroh.class, AirbendingLesson.class, KnowledgeSeeker.class})
class UncleIrohTest extends BaseCardTest {

    @Test
    void firebendingAddsRedManaUntilEndOfCombat() {
        Permanent iroh = addReadyIroh();

        declareAttacker(iroh);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void reducesLessonSpellCostByOne() {
        addReadyIroh();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void doesNotReduceNonLessonSpellCost() {
        addReadyIroh();
        harness.setHand(player1, List.of(new KnowledgeSeeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceLessonsCastByAnOpponent() {
        Permanent iroh = harness.addToBattlefieldAndReturn(player2, new UncleIroh());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, iroh.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        Permanent iroh = addReadyIroh();
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, iroh.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionStopsWhenIrohLeavesTheBattlefield() {
        Permanent iroh = addReadyIroh();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setLibrary(player1, List.of(new UncleIroh()));
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, iroh.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(iroh.getOriginalCard().getId())).isNotNull();
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firebendingResolvesAfterIrohLeavesTheBattlefield() {
        Permanent iroh = addReadyIroh();
        harness.setLibrary(player1, List.of(new UncleIroh()));
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttacker(iroh);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

            harness.castInstant(player1, 0, iroh.getId());
            harness.passBothPriorities();

            assertThat(gd.findExiledCard(iroh.getOriginalCard().getId())).isNotNull();
            resolveAllTriggers();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        });
    }

    private Permanent addReadyIroh() {
        return addCreatureReady(player1, new UncleIroh());
    }

    private void declareAttacker(Permanent attacker) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
    }
}
