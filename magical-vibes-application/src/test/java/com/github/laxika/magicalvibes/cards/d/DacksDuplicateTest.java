package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DacksDuplicate.class, GrizzlyBears.class})
class DacksDuplicateTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature and can attack immediately with dethrone")
    void copiesCreatureWithHasteAndDethrone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent duplicate = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Dack's Duplicate"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, duplicate)).isEqualTo(2);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(duplicate)));
        harness.passBothPriorities();

        assertThat(duplicate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
