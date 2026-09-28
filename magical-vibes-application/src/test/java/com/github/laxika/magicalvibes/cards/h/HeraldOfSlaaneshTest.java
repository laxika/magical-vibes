package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrinningDemon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfSlaanesh.class, GrinningDemon.class, GrizzlyBears.class})
class HeraldOfSlaaneshTest extends BaseCardTest {

    @Test
    @DisplayName("Demon spells you cast cost {2} less")
    void reducesDemonSpellCost() {
        harness.addToBattlefield(player1, new HeraldOfSlaanesh());
        harness.setHand(player1, List.of(new GrinningDemon()));
        // Grinning Demon costs {2}{B}{B}; Herald reduces the generic cost by {2}.
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grinning Demon");
    }

    @Test
    @DisplayName("Non-Demon spells are not reduced")
    void doesNotReduceNonDemonSpells() {
        harness.addToBattlefield(player1, new HeraldOfSlaanesh());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Other Demons you control have haste")
    void grantsHasteToOtherDemons() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfSlaanesh());
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new GrinningDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDemon = harness.addToBattlefieldAndReturn(player2, new GrinningDemon());

        assertThat(gqs.hasKeyword(gd, herald, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, demon, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentDemon, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A Demon cast with the cost reduction enters with haste")
    void reducedDemonEntersWithHaste() {
        harness.addToBattlefield(player1, new HeraldOfSlaanesh());
        harness.setHand(player1, List.of(new GrinningDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent demon = findPermanent(player1, "Grinning Demon");
        assertThat(gqs.hasKeyword(gd, demon, Keyword.HASTE)).isTrue();
    }
}
