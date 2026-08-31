package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MuckRats;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WickTheWhorledMind.class, MuckRats.class})
class WickTheWhorledMindTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a black Snail token when Wick enters and no Snail is controlled")
    void createsSnailWhenNoneIsControlled() {
        castWick();

        Permanent snail = findSnail();
        assertThat(snail.getEffectivePower()).isEqualTo(1);
        assertThat(snail.getEffectiveToughness()).isEqualTo(1);
        assertThat(snail.getCard().getColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on a controlled Snail when another Rat enters")
    void addsCounterToControlledSnail() {
        castWick();
        Permanent snail = findSnail();

        harness.setHand(player1, List.of(new MuckRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(snail.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Snail damages each opponent and draws cards equal to its power")
    void sacrificesSnailForDamageAndCards() {
        castWick();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SNAIL))
                .isEmpty();
    }

    private void castWick() {
        harness.setHand(player1, List.of(new WickTheWhorledMind()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findSnail() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SNAIL))
                .findFirst()
                .orElseThrow();
    }
}
