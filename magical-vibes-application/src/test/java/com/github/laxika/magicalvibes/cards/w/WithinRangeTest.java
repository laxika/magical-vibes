package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WithinRange.class, GrizzlyBears.class})
class WithinRangeTest extends BaseCardTest {

    @Test
    @DisplayName("When Within Range enters, it creates two Warrior tokens")
    void enteringCreatesTwoWarriorTokens() {
        castWithinRange();

        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(2);
        assertThat(warriors).allMatch(warrior -> warrior.getCard().isToken());
    }

    @Test
    @DisplayName("Each opponent loses life equal to the number of creatures attacking them")
    void attackCausesLifeLossForEachAttackingCreature() {
        castWithinRange();
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void castWithinRange() {
        harness.setHand(player1, List.of(new WithinRange()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
