package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BiogenicOoze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlimeAgainstHumanity.class, BiogenicOoze.class, GrizzlyBears.class})
class SlimeAgainstHumanityTest extends BaseCardTest {

    @Test
    void countsOwnedOozesAndNamedCardsInGraveyardAndExile() {
        harness.setHand(player1, List.of(new SlimeAgainstHumanity()));
        harness.setGraveyard(player1, List.of(
                new BiogenicOoze(), new SlimeAgainstHumanity(), new GrizzlyBears()));
        harness.setExile(player1, List.of(
                new BiogenicOoze(), new SlimeAgainstHumanity(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new BiogenicOoze(), new SlimeAgainstHumanity()));
        harness.setExile(player2, List.of(new BiogenicOoze(), new SlimeAgainstHumanity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent ooze = findOoze();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(ooze.getEffectivePower()).isEqualTo(6);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void createsTwoTwoOozeWithNoMatchingCards() {
        harness.setHand(player1, List.of(new SlimeAgainstHumanity()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setExile(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent ooze = findOoze();
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ooze.getEffectivePower()).isEqualTo(2);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent findOoze() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Ooze".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
