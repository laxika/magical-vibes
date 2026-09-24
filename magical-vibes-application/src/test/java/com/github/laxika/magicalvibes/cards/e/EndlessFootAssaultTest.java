package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessFootAssault.class, GrizzlyBears.class})
class EndlessFootAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        castEndlessFootAssault(List.of("{1}{W}"));
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Endless Foot Assault");
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Attacking creates a tapped Ninja attacking each opponent")
    void attackCreatesNinjaAttackingOpponent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castEndlessFootAssault(List.of());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        harness.passBothPriorities();

        List<Permanent> ninjas = findPermanents(player1, "Ninja");
        assertThat(ninjas).hasSize(1);
        assertThat(ninjas.getFirst().isTapped()).isTrue();
        assertThat(ninjas.getFirst().isAttackedThisTurn()).isTrue();
        assertThat(ninjas.getFirst().getAttackTarget()).isEqualTo(player2.getId());
    }

    private void castEndlessFootAssault(List<String> repeatedAdditionalCosts) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EndlessFootAssault()));
        harness.addMana(player1, ManaColor.COLORLESS, 2 + repeatedAdditionalCosts.size());
        harness.addMana(player1, ManaColor.WHITE, 1 + repeatedAdditionalCosts.size());

        harness.castEnchantmentWithRepeatedCosts(player1, 0, repeatedAdditionalCosts);
        harness.passBothPriorities();
    }
}
