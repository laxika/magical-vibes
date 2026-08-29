package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TenderWildguide.class)
class TenderWildguideTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new TenderWildguide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void tapAbilityAddsChosenMana() {
        addCreatureReady(player1, new TenderWildguide());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void tapAbilityPutsCounterOnThisCreature() {
        Permanent guide = addCreatureReady(player1, new TenderWildguide());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
