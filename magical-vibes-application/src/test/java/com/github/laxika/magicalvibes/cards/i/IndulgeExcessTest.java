package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndulgeExcess.class, GrizzlyBears.class, SwordsToPlowshares.class})
class IndulgeExcessTest extends BaseCardTest {

    @Test
    @DisplayName("Indulge creates a tapped and attacking Citizen for each attacking creature")
    void indulgeCreatesCitizenForEachAttackingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IndulgeExcess()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        declareAttackers(List.of(0, 1));
        resolveDelayedTokenTriggers();

        List<Permanent> tokens = findPermanents(player1, "Citizen").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Excess creates one Treasure for each own creature that dealt combat damage")
    void excessCountsCreaturesThatDealtCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new IndulgeExcess()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(findPermanents(player1, "Treasure").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Indulge");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Indulge"));
    }

    @Test
    @DisplayName("Excess counts a creature that left the battlefield after dealing combat damage")
    void excessCountsExiledDamageDealer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new IndulgeExcess()));
        harness.setHand(player1, List.of(new SwordsToPlowshares()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Excess creates no Treasure when no creature dealt combat damage to a player")
    void excessCreatesNoTreasureWithoutCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new IndulgeExcess()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Excess does not count combat damage dealt only to a blocking creature")
    void excessDoesNotCountDamageToBlocker() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new IndulgeExcess()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Indulge tokens deal combat damage and are counted by Excess")
    void citizensDealDamageAndProduceTreasure() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IndulgeExcess()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, List.of());

        declareAttackers(List.of(0));
        resolveDelayedTokenTriggers();
        resolveCombat();
        harness.assertLife(player2, 17);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Indulge spells create two Citizens per declared attacker")
    void multipleIndulgesCreateIndependentTriggers() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IndulgeExcess(), new IndulgeExcess()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castAndResolveSorcery(player1, 0, List.of());

        declareAttackers(List.of(0));
        resolveDelayedTokenTriggers();

        assertThat(countPermanents(player1, "Citizen")).isEqualTo(2);
    }

    @Test
    @DisplayName("Indulge does not create Citizens for an opponent's attackers")
    void indulgeDoesNotTriggerForOpponent() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IndulgeExcess()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, List.of());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Citizen")).isZero();
        assertThat(countPermanents(player2, "Citizen")).isZero();
    }

    private void resolveDelayedTokenTriggers() {
        while (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handlePermanentChosen(player1, player2.getId());
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
