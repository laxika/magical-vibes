package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionsFromBeyond.class, GrizzlyBears.class})
class ChampionsFromBeyondTest extends BaseCardTest {

    @Test
    void createsXHeroTokensWhenItEnters() {
        harness.setHand(player1, List.of(new ChampionsFromBeyond()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hero")).hasSize(3);
    }

    @Test
    void lightPartyScriesThenDraws() {
        addSourceAndAttackers(4);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(attackerIndices(4));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    void fullPartyBoostsOnlyTheAttackingCreaturesUntilEndOfTurn() {
        addSourceAndAttackers(8);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 100);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(attackerIndices(8));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            resolveAllTriggers();
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry) {
                gs.handleInteractionAnswer(gd, player1,
                        new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
            }
            resolveAllTriggers();
        });

        List<Permanent> attackers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isAttacking)
                .toList();
        assertThat(attackers).hasSize(8);
        assertThat(attackers).allSatisfy(attacker -> {
            assertThat(attacker.getEffectivePower()).isEqualTo(6);
            assertThat(attacker.getEffectiveToughness()).isEqualTo(6);
        });
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(attackers).allSatisfy(attacker -> {
            assertThat(attacker.getEffectivePower()).isEqualTo(2);
            assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    void fewerThanFourAttackersDoNotTrigger() {
        addSourceAndAttackers(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(attackerIndices(3));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addSourceAndAttackers(int attackerCount) {
        addCreatureReady(player1, new ChampionsFromBeyond());
        for (int i = 0; i < attackerCount; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }

    private List<Integer> attackerIndices(int attackerCount) {
        return java.util.stream.IntStream.rangeClosed(1, attackerCount).boxed().toList();
    }
}
