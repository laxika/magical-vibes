package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndulgeExcess.class, GrizzlyBears.class})
class IndulgeExcessTest extends BaseCardTest {

    @Test
    @DisplayName("Indulge creates a tapped and attacking Citizen for each attacking creature")
    void indulgeCreatesCitizenForEachAttackingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IndulgeExcess()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

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

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Indulge");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Indulge"));
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
