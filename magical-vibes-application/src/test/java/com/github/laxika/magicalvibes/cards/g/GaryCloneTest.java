package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaryClone.class, GrizzlyBears.class})
class GaryCloneTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        castGaryClone(List.of("{2}", "{2}"));
        resolveAllTriggers();

        List<Permanent> clones = findPermanents(player1, "Gary Clone");
        assertThat(clones).hasSize(3);
        assertThat(clones).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Attacking boosts each Gary Clone you control until end of turn")
    void attackingBoostsEachGaryCloneYouControl() {
        Permanent attacker = addCreatureReady(player1, new GaryClone());
        Permanent otherClone = addCreatureReady(player1, new GaryClone());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentClone = addCreatureReady(player2, new GaryClone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(otherClone.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(opponentClone.getPowerModifier()).isZero();
    }

    private void castGaryClone(List<String> repeatedAdditionalCosts) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GaryClone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1 + repeatedAdditionalCosts.size() * 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, repeatedAdditionalCosts);
        harness.passBothPriorities();
    }
}
